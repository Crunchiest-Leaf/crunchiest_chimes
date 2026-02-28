package com.crunchiest.ui;

import java.util.List;
import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import com.crunchiest.util.Messages;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

/**
 * Builds inventory items for category, sound, and paging controls.
 */
public class SoundMenuItemFactory
{
  private final NamespacedKey soundKey;
  private final NamespacedKey categoryKey;
  private final NamespacedKey actionKey;

  /**
   * Creates the item factory.
   *
   * @param plugin owning plugin
   */
  public SoundMenuItemFactory(Plugin plugin)
  {
    this.soundKey=new NamespacedKey(plugin, "selected_sound");
    this.categoryKey=new NamespacedKey(plugin, "category_name");
    this.actionKey=new NamespacedKey(plugin, "menu_action");
  }

  /**
   * Creates a category selection item.
   *
   * @param category category name
   * @param configuredIcon configured icon material name
   * @return category menu item
   */
  public ItemStack createCategoryItem(String category, String configuredIcon)
  {
    ItemStack item=new ItemStack(resolveCategoryIcon(configuredIcon));
    ItemMeta meta=item.getItemMeta();
    if (meta == null)
    {
      return item;
    }

    meta.displayName(Messages.component("<aqua><category></aqua>", Placeholder.unparsed("category", category)));
    meta.getPersistentDataContainer().set(categoryKey, PersistentDataType.STRING, category);
    List<net.kyori.adventure.text.Component> lore=java.util.Collections.singletonList(
      Messages.component("<gray>Click to view sounds</gray>")
    );
    meta.lore(lore);
    item.setItemMeta(meta);
    return item;
  }

  /**
   * Creates a sound selection item.
   *
   * @param soundName sound key
   * @return sound menu item
   */
  public ItemStack createSoundItem(String soundName)
  {
    ItemStack item=new ItemStack(Material.NOTE_BLOCK);
    ItemMeta meta=item.getItemMeta();
    if (meta == null)
    {
      return item;
    }

    meta.displayName(Messages.component("<green><sound></green>", Placeholder.unparsed("sound", soundName)));
    meta.getPersistentDataContainer().set(soundKey, PersistentDataType.STRING, soundName);
    List<net.kyori.adventure.text.Component> lore=java.util.Collections.singletonList(
      Messages.component("<gray>Click to assign this sound</gray>")
    );
    meta.lore(lore);
    item.setItemMeta(meta);
    return item;
  }

  /**
   * Creates a navigation button item.
   *
   * @param labelMiniMessage MiniMessage label
   * @param action action key
   * @return navigation menu item
   */
  public ItemStack createNavigationButton(String labelMiniMessage, String action)
  {
    ItemStack button=new ItemStack(Material.ARROW);
    ItemMeta meta=button.getItemMeta();
    if (meta == null)
    {
      return button;
    }

    meta.displayName(Messages.component(labelMiniMessage));
    meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
    button.setItemMeta(meta);
    return button;
  }

  /**
   * Creates page information item.
   *
   * @param page current page index
   * @param totalPages total page count
   * @return page info item
   */
  public ItemStack createPageInfoItem(int page, int totalPages)
  {
    ItemStack infoItem=new ItemStack(Material.PAPER);
    ItemMeta infoMeta=infoItem.getItemMeta();
    if (infoMeta == null)
    {
      return infoItem;
    }

    infoMeta.displayName(
      Messages.component(
        "<yellow>Page <page> / <total></yellow>",
        Placeholder.unparsed("page", String.valueOf(page + 1)),
        Placeholder.unparsed("total", String.valueOf(totalPages))
      )
    );
    infoItem.setItemMeta(infoMeta);
    return infoItem;
  }

  /**
   * Reads action identifier from item metadata.
   *
   * @param itemMeta item metadata
   * @return action identifier or null
   */
  public String readAction(ItemMeta itemMeta)
  {
    return itemMeta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
  }

  /**
   * Reads category identifier from item metadata.
   *
   * @param itemMeta item metadata
   * @return category identifier or null
   */
  public String readCategory(ItemMeta itemMeta)
  {
    return itemMeta.getPersistentDataContainer().get(categoryKey, PersistentDataType.STRING);
  }

  /**
   * Reads sound identifier from item metadata.
   *
   * @param itemMeta item metadata
   * @return sound identifier or null
   */
  public String readSound(ItemMeta itemMeta)
  {
    return itemMeta.getPersistentDataContainer().get(soundKey, PersistentDataType.STRING);
  }

  /**
   * Resolves a configured icon string to a Bukkit material with fallback.
   *
   * @param configuredIcon configured material string
   * @return resolved material
   */
  private Material resolveCategoryIcon(String configuredIcon)
  {
    if (configuredIcon == null)
    {
      return Material.MUSIC_DISC_13;
    }

    try
    {
      return Material.valueOf(configuredIcon.toUpperCase(Locale.ROOT));
    }
    catch (IllegalArgumentException ex)
    {
      return Material.MUSIC_DISC_13;
    }
  }
}
