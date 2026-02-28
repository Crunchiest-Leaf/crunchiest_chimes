package com.crunchiest.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.crunchiest.service.CustomJukeboxService;

/**
 * Tracks placement and removal of managed jukebox blocks.
 */
public class JukeboxLifecycleListener implements Listener
{
  private final CustomJukeboxService jukeboxService;

  /**
   * Creates the lifecycle listener.
   *
   * @param jukeboxService jukebox state service
   */
  public JukeboxLifecycleListener(CustomJukeboxService jukeboxService)
  {
    this.jukeboxService=jukeboxService;
  }

  /**
   * Registers newly placed jukeboxes.
   *
   * @param event block place event
   */
  @EventHandler(ignoreCancelled=true)
  public void onJukeboxPlace(BlockPlaceEvent event)
  {
    if (event.getBlockPlaced().getType() != Material.JUKEBOX)
    {
      return;
    }

    jukeboxService.registerJukebox(event.getBlockPlaced().getLocation());
  }

  /**
   * Unregisters broken jukeboxes.
   *
   * @param event block break event
   */
  @EventHandler(ignoreCancelled=true)
  public void onJukeboxBreak(BlockBreakEvent event)
  {
    if (event.getBlock().getType() != Material.JUKEBOX)
    {
      return;
    }

    jukeboxService.unregisterJukebox(event.getBlock().getLocation());
  }
}
