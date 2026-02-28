package com.crunchiest.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import com.crunchiest.items.ItemFactory;
import com.crunchiest.service.CustomJukeboxService;

/**
 * Breaks managed note blocks when pistons attempt to move them.
 */
public class JukeboxPistonListener implements Listener
{
  private final CustomJukeboxService jukeboxService;

  /**
   * Creates the piston listener.
   *
   * @param jukeboxService jukebox state service
   */
  public JukeboxPistonListener(CustomJukeboxService jukeboxService)
  {
    this.jukeboxService=jukeboxService;
  }

  /**
   * Handles piston extension movement.
   *
   * @param event piston extend event
   */
  @EventHandler(ignoreCancelled=true)
  public void onPistonExtend(BlockPistonExtendEvent event)
  {
    for (Block movedBlock : event.getBlocks())
    {
      breakIfCustomNoteBlock(movedBlock);
    }
  }

  /**
   * Handles piston retraction movement.
   *
   * @param event piston retract event
   */
  @EventHandler(ignoreCancelled=true)
  public void onPistonRetract(BlockPistonRetractEvent event)
  {
    for (Block movedBlock : event.getBlocks())
    {
      breakIfCustomNoteBlock(movedBlock);
    }
  }

  /**
   * Converts a moved custom note block into a dropped item and unregisters it.
   *
   * @param block moved block
   */
  private void breakIfCustomNoteBlock(Block block)
  {
    if (block.getType() != Material.NOTE_BLOCK)
    {
      return;
    }

    if (!jukeboxService.isCustomJukebox(block.getLocation()))
    {
      return;
    }

    jukeboxService.unregisterJukebox(block.getLocation());
    block.setType(Material.AIR);
    block.getWorld().dropItemNaturally(block.getLocation().add(0.5D, 0.25D, 0.5D), ItemFactory.createCustomNoteBlock());
  }
}
