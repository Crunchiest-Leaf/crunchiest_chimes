/**
 * @author Crunchiest-Leaf
 */
package com.crunchiest.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.crunchiest.CrunchiestChimes;
import com.crunchiest.service.CustomJukeboxService;
import com.crunchiest.util.Messages;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * Coordinates the custom sound selection inventory UI, including paging and menu actions.
 */
public class SoundMenuManager
{
  private static final String CATEGORY_TITLE_TEXT="Select Sound Category";
  private static final String SOUND_TITLE_PREFIX_TEXT="Sounds: ";
  private static final String CATEGORY_TITLE_MINI="<dark_aqua>Select Sound Category</dark_aqua>";
  private static final String SOUND_TITLE_MINI_PREFIX="<dark_aqua>Sounds: ";
  private static final String ACTION_PREVIOUS="previous";
  private static final String ACTION_NEXT="next";
  private static final String ACTION_BACK="back";

  private final CrunchiestChimes plugin;
  private final CustomJukeboxService jukeboxService;
  private final Map<UUID, MenuSession> menuSessionByPlayer;
  private final SoundMenuItemFactory itemFactory;

  /**
   * Creates a menu manager.
   *
   * @param plugin owning plugin
   * @param jukeboxService jukebox state service
   */
  public SoundMenuManager(CrunchiestChimes plugin, CustomJukeboxService jukeboxService)
  {
    this.plugin=plugin;
    this.jukeboxService=jukeboxService;
    this.menuSessionByPlayer=new HashMap<>();
    this.itemFactory=new SoundMenuItemFactory(plugin);
  }

  /**
   * Opens category page 0 for the given jukebox.
   *
   * @param player target player
   * @param locationKey serialized jukebox location key
   */
  public void openCategoryMenu(Player player, String locationKey)
  {
    openCategoryPage(player, locationKey, 0);
  }

  /**
   * Determines whether an inventory view belongs to this manager.
   *
   * @param view inventory view
   * @return true if the view title matches managed menu prefixes
   */
  public boolean isManagedView(InventoryView view)
  {
    // Compare plain text so title detection stays stable regardless of MiniMessage tags/colors.
    String plainTitle=PlainTextComponentSerializer.plainText().serialize(view.title());
    return plainTitle.startsWith(CATEGORY_TITLE_TEXT) || plainTitle.startsWith(SOUND_TITLE_PREFIX_TEXT);
  }

  /**
   * Clears session state for a player.
   *
   * @param playerId player UUID
   */
  public void clearSession(UUID playerId)
  {
    menuSessionByPlayer.remove(playerId);
  }

  /**
   * Handles inventory clicks in managed menus.
   *
   * @param event inventory click event
   */
  public void handleInventoryClick(InventoryClickEvent event)
  {
    if (!(event.getWhoClicked() instanceof Player))
    {
      return;
    }

    if (!isManagedView(event.getView()))
    {
      return;
    }

    Inventory clickedInventory=event.getClickedInventory();
    Inventory topInventory=event.getView().getTopInventory();
    // Ignore clicks in player inventory; only top inventory drives menu actions.
    if (clickedInventory == null || !clickedInventory.equals(topInventory))
    {
      return;
    }

    event.setCancelled(true);
    Player player=(Player) event.getWhoClicked();
    ItemStack currentItem=event.getCurrentItem();
    if (currentItem == null || currentItem.getType() == Material.AIR)
    {
      return;
    }

    ItemMeta itemMeta=currentItem.getItemMeta();
    if (itemMeta == null)
    {
      return;
    }

    MenuSession session=menuSessionByPlayer.get(player.getUniqueId());
    if (session == null || !jukeboxService.hasLocationKey(session.getLocationKey()))
    {
      player.closeInventory();
      Messages.send(player, "<red>That custom note block is no longer valid.</red>");
      return;
    }

    String action=itemFactory.readAction(itemMeta);
    // Navigation buttons are stateless and derive behavior from stored session context.
    if (ACTION_PREVIOUS.equals(action))
    {
      if (session.getViewType() == MenuViewType.CATEGORY)
      {
        openCategoryPage(player, session.getLocationKey(), session.getPage() - 1);
      }
      else
      {
        openSoundPage(player, session.getLocationKey(), session.getCategory(), session.getPage() - 1);
      }
      return;
    }

    if (ACTION_NEXT.equals(action))
    {
      if (session.getViewType() == MenuViewType.CATEGORY)
      {
        openCategoryPage(player, session.getLocationKey(), session.getPage() + 1);
      }
      else
      {
        openSoundPage(player, session.getLocationKey(), session.getCategory(), session.getPage() + 1);
      }
      return;
    }

    if (ACTION_BACK.equals(action))
    {
      openCategoryPage(player, session.getLocationKey(), 0);
      return;
    }

    if (session.getViewType() == MenuViewType.CATEGORY)
    {
      // Category clicks transition into the sound picker for that category.
      String category=itemFactory.readCategory(itemMeta);
      if (category == null || category.length() == 0)
      {
        return;
      }

      openSoundPage(player, session.getLocationKey(), category, 0);
      return;
    }

    String soundName=itemFactory.readSound(itemMeta);
    if (soundName == null || soundName.length() == 0)
    {
      return;
    }

    jukeboxService.setSelectedSound(session.getLocationKey(), soundName);
    player.closeInventory();
    Messages.send(player, "<green>Selected sound: <aqua><sound></aqua></green>", Placeholder.unparsed("sound", soundName));
  }

  /**
   * Opens a paged category menu.
   *
   * @param player target player
   * @param locationKey serialized jukebox location key
   * @param requestedPage requested page index
   */
  private void openCategoryPage(Player player, String locationKey, int requestedPage)
  {
    ConfigurationSection section=plugin.getConfig().getConfigurationSection("sounds");
    if (section == null || section.getKeys(false).isEmpty())
    {
      Messages.send(player, "<red>No sounds are configured. Check config.yml.</red>");
      return;
    }

    List<String> categories=new ArrayList<>(section.getKeys(false));
    int totalPages=PagedMenuLayout.totalPages(categories.size());
    int page=PagedMenuLayout.clampPage(requestedPage, totalPages);
    int startIndex=PagedMenuLayout.startIndex(page);
    // Title includes current page for clarity when browsing large category lists.
    Inventory inventory=Bukkit.createInventory(
      null,
      PagedMenuLayout.INVENTORY_SIZE,
      Messages.component(
        CATEGORY_TITLE_MINI + " <gray>(<page>/<total>)</gray>",
        Placeholder.unparsed("page", String.valueOf(page + 1)),
        Placeholder.unparsed("total", String.valueOf(totalPages))
      )
    );

    for (int i=0; i < PagedMenuLayout.CONTENT_SLOTS && startIndex + i < categories.size(); i++)
    {
      // Render only the window of categories for the active page.
      String category=categories.get(startIndex + i);
      String iconName=plugin.getConfig().getString("category-icons." + category);
      ItemStack item=itemFactory.createCategoryItem(category, iconName);
      inventory.setItem(i, item);
    }

    addNavigationControls(inventory, page, totalPages, false);

    menuSessionByPlayer.put(player.getUniqueId(), new MenuSession(locationKey, MenuViewType.CATEGORY, null, page));
    player.openInventory(inventory);
  }

  /**
   * Opens a paged sound menu for a category.
   *
   * @param player target player
   * @param locationKey serialized jukebox location key
   * @param category selected category
   * @param requestedPage requested page index
   */
  private void openSoundPage(Player player, String locationKey, String category, int requestedPage)
  {
    List<String> sounds=plugin.getConfig().getStringList("sounds." + category);
    if (sounds.isEmpty())
    {
      Messages.send(
        player,
        "<yellow>No sounds configured in category: <gold><category></gold></yellow>",
        Placeholder.unparsed("category", category)
      );
      return;
    }

    int totalPages=PagedMenuLayout.totalPages(sounds.size());
    int page=PagedMenuLayout.clampPage(requestedPage, totalPages);
    int startIndex=PagedMenuLayout.startIndex(page);
    // Keep sound menu layout identical to category menu for predictable UX.
    Inventory inventory=Bukkit.createInventory(
      null,
      PagedMenuLayout.INVENTORY_SIZE,
      Messages.component(
        SOUND_TITLE_MINI_PREFIX + "<aqua><category></aqua> <gray>(<page>/<total>)</gray>",
        Placeholder.unparsed("category", category),
        Placeholder.unparsed("page", String.valueOf(page + 1)),
        Placeholder.unparsed("total", String.valueOf(totalPages))
      )
    );

    for (int i=0; i < PagedMenuLayout.CONTENT_SLOTS && startIndex + i < sounds.size(); i++)
    {
      // Render only the sounds for the selected page window.
      String soundName=sounds.get(startIndex + i);
      ItemStack item=itemFactory.createSoundItem(soundName);
      inventory.setItem(i, item);
    }

    addNavigationControls(inventory, page, totalPages, true);

    menuSessionByPlayer.put(player.getUniqueId(), new MenuSession(locationKey, MenuViewType.SOUND, category, page));
    player.openInventory(inventory);
  }

  /**
   * Adds navigation controls to the menu footer row.
   *
   * @param inventory target inventory
   * @param page current page index
   * @param totalPages total page count
   * @param showBackButton whether to render the back button
   */
  private void addNavigationControls(Inventory inventory, int page, int totalPages, boolean showBackButton)
  {
    // Page info is always shown to keep context when navigating multiple pages.
    inventory.setItem(PagedMenuLayout.PAGE_INFO_SLOT, itemFactory.createPageInfoItem(page, totalPages));

    if (page > 0)
    {
      inventory.setItem(PagedMenuLayout.PREVIOUS_SLOT, itemFactory.createNavigationButton("<gold>Previous Page</gold>", ACTION_PREVIOUS));
    }

    if (showBackButton)
    {
      inventory.setItem(PagedMenuLayout.BACK_SLOT, itemFactory.createNavigationButton("<aqua>Back to Categories</aqua>", ACTION_BACK));
    }

    if (page < totalPages - 1)
    {
      inventory.setItem(PagedMenuLayout.NEXT_SLOT, itemFactory.createNavigationButton("<gold>Next Page</gold>", ACTION_NEXT));
    }
  }
}
