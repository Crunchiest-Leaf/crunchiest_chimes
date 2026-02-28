package com.crunchiest.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.crunchiest.CrunchiestChimes;
import com.crunchiest.util.LocationKey;

/**
 * SQLite-backed repository for persistent custom jukebox sound selections.
 */
public class SqliteJukeboxRepository
{
  private final CrunchiestChimes plugin;
  private Connection connection;

  /**
   * Creates a repository instance.
   *
   * @param plugin owning plugin
   */
  public SqliteJukeboxRepository(CrunchiestChimes plugin)
  {
    this.plugin=plugin;
  }

  /**
   * Opens database connection and creates required schema.
   *
   * @throws SQLException if schema initialization fails
   */
  public void initialize() throws SQLException
  {
    // Ensure plugin data folder exists before opening/creating the SQLite file.
    if (!plugin.getDataFolder().exists())
    {
      plugin.getDataFolder().mkdirs();
    }

    File dbFile=new File(plugin.getDataFolder(), "chimes.db");
    // Keep a single long-lived connection for lightweight plugin persistence usage.
    connection=DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

    String sql="CREATE TABLE IF NOT EXISTS custom_jukeboxes ("
      + "world TEXT NOT NULL,"
      + "x INTEGER NOT NULL,"
      + "y INTEGER NOT NULL,"
      + "z INTEGER NOT NULL,"
      + "selected_sound TEXT NOT NULL DEFAULT '',"
      + "PRIMARY KEY(world, x, y, z)"
      + ")";

    try (Statement statement=connection.createStatement())
    {
      // Schema setup is idempotent and safe on every startup.
      statement.execute(sql);
    }
  }

  /**
   * Inserts a jukebox record when it does not exist.
   *
   * @param key location key
   * @throws SQLException if insertion fails
   */
  public void upsertJukebox(LocationKey key) throws SQLException
  {
    String sql="INSERT OR IGNORE INTO custom_jukeboxes(world, x, y, z, selected_sound) VALUES(?, ?, ?, ?, '')";
    try (PreparedStatement statement=connection.prepareStatement(sql))
    {
      statement.setString(1, key.getWorldName());
      statement.setInt(2, key.getX());
      statement.setInt(3, key.getY());
      statement.setInt(4, key.getZ());
      statement.executeUpdate();
    }
  }

  /**
   * Removes a jukebox record.
   *
   * @param key location key
   * @throws SQLException if deletion fails
   */
  public void removeJukebox(LocationKey key) throws SQLException
  {
    String sql="DELETE FROM custom_jukeboxes WHERE world = ? AND x = ? AND y = ? AND z = ?";
    try (PreparedStatement statement=connection.prepareStatement(sql))
    {
      statement.setString(1, key.getWorldName());
      statement.setInt(2, key.getX());
      statement.setInt(3, key.getY());
      statement.setInt(4, key.getZ());
      statement.executeUpdate();
    }
  }

  /**
   * Updates selected sound for a jukebox record.
   *
   * @param key location key
   * @param sound selected sound key
   * @throws SQLException if update fails
   */
  public void setSelectedSound(LocationKey key, String sound) throws SQLException
  {
    String sql="UPDATE custom_jukeboxes SET selected_sound = ? WHERE world = ? AND x = ? AND y = ? AND z = ?";
    try (PreparedStatement statement=connection.prepareStatement(sql))
    {
      statement.setString(1, sound);
      statement.setString(2, key.getWorldName());
      statement.setInt(3, key.getX());
      statement.setInt(4, key.getY());
      statement.setInt(5, key.getZ());
      statement.executeUpdate();
    }
  }

  /**
   * Loads all jukebox records into a map keyed by serialized location.
   *
   * @return location-to-sound map
   * @throws SQLException if query fails
   */
  public Map<String, String> loadAllJukeboxes() throws SQLException
  {
    Map<String, String> result=new HashMap<>();
    String sql="SELECT world, x, y, z, selected_sound FROM custom_jukeboxes";
    try (Statement statement=connection.createStatement(); ResultSet resultSet=statement.executeQuery(sql))
    {
      // Materialize result set into the in-memory cache-friendly map format.
      while (resultSet.next())
      {
        LocationKey key=new LocationKey(
          resultSet.getString("world"),
          resultSet.getInt("x"),
          resultSet.getInt("y"),
          resultSet.getInt("z")
        );
        result.put(key.asString(), resultSet.getString("selected_sound"));
      }
    }
    return result;
  }

  /**
   * Closes database connection.
   */
  public void close()
  {
    if (connection == null)
    {
      return;
    }

    try
    {
      connection.close();
    }
    catch (SQLException ex)
    {
      plugin.getLogger().warning(String.format("Failed closing SQLite connection: %s", ex.getMessage()));
    }
  }
}
