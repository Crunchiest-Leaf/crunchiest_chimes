package com.crunchiest.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.bukkit.Location;

import com.crunchiest.CrunchiestChimes;
import com.crunchiest.storage.SqliteJukeboxRepository;
import com.crunchiest.util.LocationKey;

/**
 * In-memory note block state service with asynchronous SQLite persistence.
 */
public class CustomJukeboxService
{
  private static final long SHUTDOWN_TIMEOUT_SECONDS=10L;
  private static final int WRITE_QUEUE_CAPACITY=5000;
  private static final long REJECTED_LOG_INTERVAL_MS=30000L;
  private static final long RETRY_DELAY_TICKS=20L;
  private static final int RETRY_ATTEMPTS=1;

  private final CrunchiestChimes plugin;
  private final SqliteJukeboxRepository repository;
  private final Map<String, String> selectedSoundsByLocation;
  private final Set<String> poweredLocations;
  private final ThreadPoolExecutor databaseWriteExecutor;
  private final AtomicLong droppedWriteCount;

  private volatile long lastRejectedLogTimeMs;

  /**
   * Creates the service and loads persisted note block state.
   *
   * @param plugin owning plugin
   * @param repository SQLite repository
   * @throws SQLException if initial state loading fails
   */
  public CustomJukeboxService(CrunchiestChimes plugin, SqliteJukeboxRepository repository) throws SQLException
  {
    this.plugin=plugin;
    this.repository=repository;
    this.selectedSoundsByLocation=new HashMap<>(repository.loadAllJukeboxes());
    this.poweredLocations=new HashSet<>();
    this.droppedWriteCount=new AtomicLong(0L);
    this.lastRejectedLogTimeMs=0L;
    this.databaseWriteExecutor=createDatabaseWriteExecutor();
  }

  /**
   * Registers a newly placed custom note block.
   *
   * @param location note block location
   */
  public void registerJukebox(Location location)
  {
    LocationKey key=LocationKey.fromLocation(location);
    String keyText=key.asString();
    if (selectedSoundsByLocation.containsKey(keyText))
    {
      return;
    }

    // Update cache first so gameplay behavior is immediate on main thread.
    selectedSoundsByLocation.put(keyText, "");
    // Persistence is best-effort async; gameplay should not block on SQLite I/O.
    enqueueWrite(() -> repository.upsertJukebox(key));
  }

  /**
   * Unregisters a broken custom note block.
   *
   * @param location note block location
   */
  public void unregisterJukebox(Location location)
  {
    LocationKey key=LocationKey.fromLocation(location);
    String keyText=key.asString();
    // Remove from all in-memory indexes before scheduling persistent cleanup.
    selectedSoundsByLocation.remove(keyText);
    poweredLocations.remove(keyText);
    enqueueWrite(() -> repository.removeJukebox(key));
  }

  /**
   * Checks whether a location is a managed custom note block.
   *
   * @param location block location
   * @return true when managed
   */
  public boolean isCustomJukebox(Location location)
  {
    return selectedSoundsByLocation.containsKey(LocationKey.fromLocation(location).asString());
  }

  /**
   * Converts a Bukkit location into the internal location key string.
   *
   * @param location block location
   * @return serialized location key
   */
  public String getLocationKey(Location location)
  {
    return LocationKey.fromLocation(location).asString();
  }

  /**
   * Checks whether an internal location key exists.
   *
   * @param locationKey serialized location key
   * @return true when key exists
   */
  public boolean hasLocationKey(String locationKey)
  {
    return selectedSoundsByLocation.containsKey(locationKey);
  }

  /**
   * Sets selected sound for a managed note block and queues persistence.
   *
   * @param locationKey serialized location key
   * @param soundName selected sound key
   */
  public void setSelectedSound(String locationKey, String soundName)
  {
    LocationKey key=LocationKey.fromString(locationKey);
    // Cache is source-of-truth for runtime reads; DB durability is queued.
    selectedSoundsByLocation.put(locationKey, soundName);
    enqueueWrite(() -> repository.setSelectedSound(key, soundName));
  }

  /**
   * Gets selected sound for a managed note block.
   *
   * @param locationKey serialized location key
   * @return selected sound or empty string
   */
  public String getSelectedSound(String locationKey)
  {
    String value=selectedSoundsByLocation.get(locationKey);
    return value == null ? "" : value;
  }

  /**
   * Marks a location as powered and returns true only on rising edge.
   *
   * @param locationKey serialized location key
   * @return true when this is the first powered event since last clear
   */
  public boolean markPoweredIfRisingEdge(String locationKey)
  {
    return poweredLocations.add(locationKey);
  }

  /**
   * Clears powered state for a location.
   *
   * @param locationKey serialized location key
   */
  public void clearPowered(String locationKey)
  {
    poweredLocations.remove(locationKey);
  }

  /**
   * Flushes queued persistence tasks and shuts down async workers.
   */
  public void shutdown()
  {
    // Stop accepting new writes and wait briefly for queued tasks to complete.
    databaseWriteExecutor.shutdown();
    try
    {
      if (!databaseWriteExecutor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS))
      {
        plugin.getLogger().warning("Timed out waiting for SQLite write queue to flush.");
        databaseWriteExecutor.shutdownNow();
      }
    }
    catch (InterruptedException ex)
    {
      Thread.currentThread().interrupt();
      databaseWriteExecutor.shutdownNow();
    }

    long dropped=droppedWriteCount.get();
    if (dropped > 0)
    {
      plugin.getLogger().warning(String.format("Dropped %d SQLite write(s) due to queue pressure.", dropped));
    }
  }

  /**
   * Creates the single-thread SQLite write executor.
   *
   * @return configured executor
   */
  private ThreadPoolExecutor createDatabaseWriteExecutor()
  {
    ThreadFactory threadFactory=runnable -> {
      Thread thread=new Thread(runnable, "CrunchiestChimes-SQLite-Writer");
      thread.setDaemon(true);
      return thread;
    };

    // Single-thread executor preserves write ordering for a given location key.
    return new ThreadPoolExecutor(
      1,
      1,
      0L,
      TimeUnit.MILLISECONDS,
      new ArrayBlockingQueue<>(WRITE_QUEUE_CAPACITY),
      threadFactory,
      new ThreadPoolExecutor.AbortPolicy()
    );
  }

  /**
   * Wraps and enqueues a SQL write task with retry metadata.
   *
   * @param sqlTask SQL write callback
   */
  private void enqueueWrite(SqlTask sqlTask)
  {
    // Wrap SQL work with retry metadata so queue pressure can be handled gracefully.
    submitWriteTask(new SqlWriteTask(sqlTask, RETRY_ATTEMPTS));
  }

  /**
   * Submits a write task to executor or handles rejections.
   *
   * @param writeTask write task
   */
  private void submitWriteTask(SqlWriteTask writeTask)
  {
    if (databaseWriteExecutor.isShutdown())
    {
      incrementDroppedWrites("executor shutting down");
      return;
    }

    try
    {
      databaseWriteExecutor.execute(writeTask);
    }
    catch (RejectedExecutionException ex)
    {
      // Queue saturation is expected under pressure; route through retry/drop policy.
      handleRejectedWrite(writeTask);
    }
  }

  /**
   * Handles queue rejection with delayed retry and eventual drop accounting.
   *
   * @param writeTask rejected task
   */
  private void handleRejectedWrite(SqlWriteTask writeTask)
  {
    if (databaseWriteExecutor.isShutdown())
    {
      incrementDroppedWrites("executor shutdown");
      return;
    }

    if (writeTask.hasRetriesLeft())
    {
      // Retry shortly in case queue pressure is transient.
      SqlWriteTask retryTask=writeTask.createRetryTask();
      plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> submitWriteTask(retryTask), RETRY_DELAY_TICKS);
      return;
    }

    // Final fallback: drop and account for telemetry/logging.
    incrementDroppedWrites("queue full");
  }

  /**
   * Increments dropped-write counters with rate-limited warning output.
   *
   * @param reason reason label for dropped write
   */
  private void incrementDroppedWrites(String reason)
  {
    long dropped=droppedWriteCount.incrementAndGet();
    long now=System.currentTimeMillis();
    if (now - lastRejectedLogTimeMs >= REJECTED_LOG_INTERVAL_MS)
    {
      lastRejectedLogTimeMs=now;
      plugin.getLogger().warning(
        String.format(
          "SQLite write dropped (%s). Total dropped: %d, queueSize=%d",
          reason,
          dropped,
          databaseWriteExecutor.getQueue().size()
        )
      );
    }
  }

  /** SQL write callback abstraction used by the async persistence queue. */
  private interface SqlTask
  {
    /**
     * Executes a SQL operation.
     *
     * @throws SQLException on database error
     */
    void run() throws SQLException;
  }

  /**
   * Runnable wrapper that carries retry metadata for queued SQL work.
   */
  private final class SqlWriteTask implements Runnable
  {
    private final SqlTask sqlTask;
    private final int retriesLeft;

    /**
     * Creates a SQL write task wrapper.
     *
     * @param sqlTask SQL operation callback
     * @param retriesLeft remaining retry attempts
     */
    private SqlWriteTask(SqlTask sqlTask, int retriesLeft)
    {
      this.sqlTask=sqlTask;
      this.retriesLeft=retriesLeft;
    }

    /**
     * Indicates whether this task can be retried.
     *
     * @return true when retry attempts remain
     */
    private boolean hasRetriesLeft()
    {
      return retriesLeft > 0;
    }

    /**
     * Creates a copy with one fewer retry attempt.
     *
     * @return retry task
     */
    private SqlWriteTask createRetryTask()
    {
      return new SqlWriteTask(sqlTask, retriesLeft - 1);
    }

    /** Executes the wrapped SQL task and logs failures. */
    @Override
    public void run()
    {
      try
      {
        sqlTask.run();
      }
      catch (SQLException ex)
      {
        plugin.getLogger().warning(String.format("SQLite async write failed: %s", ex.getMessage()));
      }
    }
  }
}
