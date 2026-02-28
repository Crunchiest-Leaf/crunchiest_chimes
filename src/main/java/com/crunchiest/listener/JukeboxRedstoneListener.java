package com.crunchiest.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

import com.crunchiest.service.RedstonePlaybackService;

/**
 * Routes redstone updates on jukebox blocks to the playback service.
 */
public class JukeboxRedstoneListener implements Listener
{
  private final RedstonePlaybackService redstonePlaybackService;

  /**
   * Creates the redstone listener.
   *
   * @param redstonePlaybackService playback service for redstone signals
   */
  public JukeboxRedstoneListener(RedstonePlaybackService redstonePlaybackService)
  {
    this.redstonePlaybackService=redstonePlaybackService;
  }

  /**
   * Handles redstone state changes on jukebox blocks.
   *
   * @param event redstone event
   */
  @EventHandler
  public void onJukeboxRedstone(BlockRedstoneEvent event)
  {
    if (event.getBlock().getType() != Material.JUKEBOX)
    {
      return;
    }

    redstonePlaybackService.handleRedstoneSignal(event.getBlock(), event.getNewCurrent());
  }
}
