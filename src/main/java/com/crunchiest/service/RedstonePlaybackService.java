/**
 * @author Crunchiest-Leaf
 */
package com.crunchiest.service;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.NoteBlock;

import com.crunchiest.CrunchiestChimes;

/**
 * Plays configured custom sounds when managed note blocks receive redstone power.
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
   * Processes a redstone signal update for a managed note block.
   *
   * @param block note block
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

    playCurrentSound(block);
  }

  /**
   * Plays the managed custom sound for a note block at its current configured pitch.
   *
   * @param block note block
   */
  public void playCurrentSound(Block block)
  {
    String locationKey=jukeboxService.getLocationKey(block.getLocation());
    if (!jukeboxService.hasLocationKey(locationKey))
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
    float basePitch=(float) plugin.getConfig().getDouble("playback.pitch", 1.0D);
    float pitch=basePitch * resolveNoteBlockPitchMultiplier(block);
    Location location=block.getLocation().add(0.5D, 0.5D, 0.5D);
    World world=block.getWorld();
    world.playSound(location, soundName, volume, pitch);
  }

  /**
   * Resolves a pitch multiplier from the note block's current note value.
   *
   * @param block note block
   * @return pitch multiplier in the same range as vanilla note blocks
   */
  private float resolveNoteBlockPitchMultiplier(Block block)
  {
    BlockData blockData=block.getBlockData();
    if (!(blockData instanceof NoteBlock))
    {
      return 1.0F;
    }

    NoteBlock noteBlock=(NoteBlock) blockData;
    int noteId=noteBlock.getNote().getId();
    return (float) Math.pow(2.0D, (noteId - 12) / 12.0D);
  }
}
