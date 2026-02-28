package com.crunchiest.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.crunchiest.service.CustomJukeboxService;
import com.crunchiest.ui.SoundMenuManager;

/**
 * Intercepts right-click interactions on managed jukeboxes and opens the sound menu.
 */
public class JukeboxInteractionListener implements Listener
{
  private final CustomJukeboxService jukeboxService;
  private final SoundMenuManager menuManager;

  /**
   * Creates the interaction listener.
   *
   * @param jukeboxService jukebox state service
   * @param menuManager sound menu manager
   */
  public JukeboxInteractionListener(CustomJukeboxService jukeboxService, SoundMenuManager menuManager)
  {
    this.jukeboxService=jukeboxService;
    this.menuManager=menuManager;
  }

  /**
   * Cancels normal jukebox interaction and opens the category menu for managed jukeboxes.
   *
   * @param event player interaction event
   */
  @EventHandler
  public void onJukeboxInteract(PlayerInteractEvent event)
  {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
    {
      return;
    }

    Block clickedBlock=event.getClickedBlock();
    if (clickedBlock == null || clickedBlock.getType() != Material.JUKEBOX)
    {
      return;
    }

    if (!jukeboxService.isCustomJukebox(clickedBlock.getLocation()))
    {
      return;
    }

    event.setUseInteractedBlock(Event.Result.DENY);
    event.setUseItemInHand(Event.Result.DENY);
    event.setCancelled(true);

    if (event.getHand() != EquipmentSlot.HAND)
    {
      return;
    }

    String locationKey=jukeboxService.getLocationKey(clickedBlock.getLocation());
    menuManager.openCategoryMenu(event.getPlayer(), locationKey);
  }
}
