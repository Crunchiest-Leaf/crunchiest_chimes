package com.crunchiest.service;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.crunchiest.CrunchiestChimes;

/**
 * Plays configured custom sounds when managed jukeboxes receive redstone power.
 */
public class RedstonePlaybackService
{
  private final CrunchiestChimes plugin;
  private final CustomJukeboxService jukeboxService;

  /**
   * Creates a playback service.
   *
   * @param plugin owning plugin
   * @param jukeboxService jukebox state service
   */
  public RedstonePlaybackService(CrunchiestChimes plugin, CustomJukeboxService jukeboxService)
  {
    this.plugin=plugin;
    this.jukeboxService=jukeboxService;
  }

  /**
   * Processes a redstone signal update for a jukebox block.
   *
   * @param block jukebox block
   * @param newCurrent new redstone current value
   */
  public void handleRedstoneSignal(Block block, int newCurrent)
  {
    String locationKey=jukeboxService.getLocationKey(block.getLocation());
    if (!jukeboxService.hasLocationKey(locationKey))
    {
      return;
    }

    boolean powered=newCurrent > 0;
    if (!powered)
    {
      jukeboxService.clearPowered(locationKey);
      return;
    }

    if (!jukeboxService.markPoweredIfRisingEdge(locationKey))
    {
      return;
    }

    String soundName=jukeboxService.getSelectedSound(locationKey);
    if (soundName.length() == 0)
    {
      soundName=plugin.getConfig().getString("default-sound", "");
    }

    if (soundName == null || soundName.length() == 0)
    {
      return;
    }

    float volume=(float) plugin.getConfig().getDouble("playback.volume", 1.0D);
    float pitch=(float) plugin.getConfig().getDouble("playback.pitch", 1.0D);
    Location location=block.getLocation().add(0.5D, 0.5D, 0.5D);
    World world=block.getWorld();
    world.playSound(location, soundName, volume, pitch);
  }
}
