package com.crunchiest.listener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.crunchiest.items.ItemFactory;
import com.crunchiest.service.CustomJukeboxService;

/**
 * Tracks placement and removal of managed note block chime blocks.
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
   * Registers newly placed note blocks.
   *
   * @param event block place event
   */
  @EventHandler(ignoreCancelled=true)
  public void onJukeboxPlace(BlockPlaceEvent event)
  {
    if (event.getBlockPlaced().getType() != Material.NOTE_BLOCK)
    {
      return;
    }

    ItemStack itemInHand=event.getItemInHand();
    if (!ItemFactory.hasCustomItemTag(itemInHand, ItemFactory.CUSTOM_NOTE_BLOCK_TAG))
    {
      return;
    }

    jukeboxService.registerJukebox(event.getBlockPlaced().getLocation());
  }

  /**
   * Unregisters broken note blocks.
   *
   * @param event block break event
   */
  @EventHandler(ignoreCancelled=true)
  public void onJukeboxBreak(BlockBreakEvent event)
  {
    if (event.getBlock().getType() != Material.NOTE_BLOCK)
    {
      return;
    }

    if (!jukeboxService.isCustomJukebox(event.getBlock().getLocation()))
    {
      // Ignore vanilla note blocks entirely.
      return;
    }

    // Prevent vanilla note block drops so custom block yields exactly its tagged item.
    event.setDropItems(false);

    jukeboxService.unregisterJukebox(event.getBlock().getLocation());

    if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
    {
      return;
    }

    ItemStack customBlock=ItemFactory.createCustomNoteBlock();
    var leftovers=event.getPlayer().getInventory().addItem(customBlock);
    if (!leftovers.isEmpty())
    {
      leftovers.values().forEach(item -> event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item));
    }
  }
}
