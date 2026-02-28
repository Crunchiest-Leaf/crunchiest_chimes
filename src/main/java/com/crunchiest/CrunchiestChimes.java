/**
 * @author Crunchiest-Leaf
 */
package com.crunchiest;

import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.crunchiest.command.ChimesCommand;
import com.crunchiest.items.ItemFactory;
import com.crunchiest.listener.CustomCraftingListener;
import com.crunchiest.listener.JukeboxInteractionListener;
import com.crunchiest.listener.JukeboxLifecycleListener;
import com.crunchiest.listener.JukeboxPistonListener;
import com.crunchiest.listener.JukeboxRedstoneListener;
import com.crunchiest.listener.MenuListener;
import com.crunchiest.recipes.listenerRecipes.CustomRecipeBuilder;
import com.crunchiest.recipes.listenerRecipes.CustomRecipeRegistry;
import com.crunchiest.service.CustomJukeboxService;
import com.crunchiest.service.RedstonePlaybackService;
import com.crunchiest.storage.SqliteJukeboxRepository;
import com.crunchiest.ui.SoundMenuManager;

/**
 * Main Paper plugin bootstrap for CrunchiestChimes.
 */
public class CrunchiestChimes extends JavaPlugin
{
  private NamespacedKey pluginDefaultKey;
  private SqliteJukeboxRepository repository;
  private CustomJukeboxService jukeboxService;

  /**
   * Initializes storage, services, commands, and listeners.
   */
  @Override
  public void onEnable()
  {
    saveDefaultConfig();
    pluginDefaultKey=new NamespacedKey(this, "custom_item");
    ItemFactory.initialize(this);
    CustomRecipeBuilder.initialize(this);
    CustomRecipeRegistry.registerAllRecipes();

    try
    {
      repository=new SqliteJukeboxRepository(this);
      repository.initialize();
      jukeboxService=new CustomJukeboxService(this, repository);
    }
    catch (SQLException ex)
    {
      getLogger().log(Level.SEVERE, "Failed to initialize SQLite storage", ex);
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    SoundMenuManager soundMenuManager=new SoundMenuManager(this, jukeboxService);
    RedstonePlaybackService redstonePlaybackService=new RedstonePlaybackService(this, jukeboxService);
    ChimesCommand commandHandler=new ChimesCommand(this, jukeboxService, soundMenuManager);

    PluginCommand chimesCommand=getCommand("chimes");
    if (chimesCommand != null)
    {
      chimesCommand.setExecutor(commandHandler);
    }

    PluginManager pluginManager=getServer().getPluginManager();
    pluginManager.registerEvents(new JukeboxLifecycleListener(jukeboxService), this);
    pluginManager.registerEvents(new JukeboxPistonListener(jukeboxService), this);
    pluginManager.registerEvents(new JukeboxInteractionListener(jukeboxService, redstonePlaybackService, soundMenuManager), this);
    pluginManager.registerEvents(new JukeboxRedstoneListener(redstonePlaybackService), this);
    pluginManager.registerEvents(new MenuListener(soundMenuManager), this);
    pluginManager.registerEvents(new CustomCraftingListener(), this);
    getLogger().info("crunchiest_chimes enabled");
  }

  /**
   * Shuts down asynchronous services and closes storage resources.
   */
  @Override
  public void onDisable()
  {
    CustomRecipeRegistry.unregisterAllRecipes();

    if (jukeboxService != null)
    {
      jukeboxService.shutdown();
    }

    if (repository != null)
    {
      repository.close();
    }
    getLogger().info("crunchiest_chimes disabled");
  }

  /**
   * Returns the default plugin key used for item PDC tagging.
   *
   * @return plugin-scoped data key
   */
  public NamespacedKey getPluginDefaultKey()
  {
    return pluginDefaultKey;
  }
}
