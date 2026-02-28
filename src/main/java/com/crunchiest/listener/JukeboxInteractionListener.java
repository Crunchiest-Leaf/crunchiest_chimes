/**
 * @author Crunchiest-Leaf
 */
package com.crunchiest.listener;

import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.crunchiest.service.CustomJukeboxService;
import com.crunchiest.service.RedstonePlaybackService;
import com.crunchiest.ui.SoundMenuManager;

/**
 * Intercepts right-click interactions on managed note blocks and opens the sound menu.
 */
public class JukeboxInteractionListener implements Listener
{
  private final CustomJukeboxService jukeboxService;
  private final RedstonePlaybackService redstonePlaybackService;
  private final SoundMenuManager menuManager;

  /**
   * Creates the interaction listener.
   *
   * @param jukeboxService jukebox state service
   * @param redstonePlaybackService sound playback service
   * @param menuManager sound menu manager
   */
  public JukeboxInteractionListener(
    CustomJukeboxService jukeboxService,
    RedstonePlaybackService redstonePlaybackService,
    SoundMenuManager menuManager
  )
  {
    this.jukeboxService=jukeboxService;
    this.redstonePlaybackService=redstonePlaybackService;
    this.menuManager=menuManager;
  }

  /**
   * Cancels normal note block interaction and opens the category menu for managed note blocks.
   *
   * @param event player interaction event
   */
  @EventHandler
  public void onJukeboxInteract(PlayerInteractEvent event)
  {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
    {
      return;
    }

    Block clickedBlock=event.getClickedBlock();
    if (clickedBlock == null || clickedBlock.getType() != Material.NOTE_BLOCK)
    {
      return;
    }

    if (!jukeboxService.isCustomJukebox(clickedBlock.getLocation()))
    {
      return;
    }

    if (event.getHand() != EquipmentSlot.HAND)
    {
      return;
    }

    if (event.getAction() == Action.LEFT_CLICK_BLOCK)
    {
      event.setUseInteractedBlock(Event.Result.DENY);
      event.setUseItemInHand(Event.Result.DENY);
      event.setCancelled(true);
      redstonePlaybackService.playCurrentSound(clickedBlock);
      return;
    }

    if (event.getPlayer().isSneaking())
    {
      event.setUseInteractedBlock(Event.Result.DENY);
      event.setUseItemInHand(Event.Result.DENY);
      event.setCancelled(true);
      cyclePitch(clickedBlock);
      return;
    }

    event.setUseInteractedBlock(Event.Result.DENY);
    event.setUseItemInHand(Event.Result.DENY);
    event.setCancelled(true);

    String locationKey=jukeboxService.getLocationKey(clickedBlock.getLocation());
    menuManager.openCategoryMenu(event.getPlayer(), locationKey);
  }

  /**
   * Advances the managed note block's note value by one, wrapping at the end.
   *
   * @param block note block
   */
  private void cyclePitch(Block block)
  {
    BlockData blockData=block.getBlockData();
    if (!(blockData instanceof NoteBlock))
    {
      return;
    }

    NoteBlock noteBlock=(NoteBlock) blockData;
    int nextNoteId=(noteBlock.getNote().getId() + 1) % 25;
    noteBlock.setNote(new Note(nextNoteId));
    block.setBlockData(noteBlock);
  }
}
