/**
 * @author Crunchiest-Leaf
 */
package com.crunchiest.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.crunchiest.CrunchiestChimes;
import com.crunchiest.util.Messages;

/**
 * Factory for plugin custom items.
 */
public final class ItemFactory
{
  public static final String CUSTOM_NOTE_BLOCK_TAG="custom_note_block";

  private static NamespacedKey persistentDataKey;

  private ItemFactory()
  {
  }

  /**
   * Initializes factory dependencies.
   *
   * @param plugin plugin instance
   */
  public static void initialize(CrunchiestChimes plugin)
  {
    persistentDataKey=plugin.getPluginDefaultKey();
  }

  /**
   * Creates the custom craftable note block item.
   *
   * @return custom note block item
   */
  public static ItemStack createCustomNoteBlock()
  {
    ItemStack noteBlock=new ItemStack(Material.NOTE_BLOCK);
    ItemMeta meta=noteBlock.getItemMeta();
    if (meta == null)
    {
      return noteBlock;
    }

    meta.displayName(Messages.component("<aqua>Resonant Note Block</aqua>"));
    List<net.kyori.adventure.text.Component> lore=Arrays.asList(
      Messages.component("<gray>A crafted chime block for CrunchiestChimes.</gray>"),
      Messages.component("<dark_gray>Shift + Right Click: cycle pitch</dark_gray>"),
      Messages.component("<dark_gray>Left Click: preview current sound</dark_gray>")
    );
    meta.lore(lore);
    setPersistentData(meta, CUSTOM_NOTE_BLOCK_TAG);
    noteBlock.setItemMeta(meta);
    return noteBlock;
  }

  /**
   * Checks whether an item has the plugin custom tag value.
   *
   * @param item item to inspect
   * @param expectedValue expected tag value
   * @return true if item is tagged with expected value
   */
  public static boolean hasCustomItemTag(ItemStack item, String expectedValue)
  {
    if (item == null || !item.hasItemMeta() || persistentDataKey == null)
    {
      return false;
    }

    ItemMeta meta=item.getItemMeta();
    if (meta == null)
    {
      return false;
    }

    PersistentDataContainer data=meta.getPersistentDataContainer();
    if (!data.has(persistentDataKey, PersistentDataType.STRING))
    {
      return false;
    }

    String value=data.get(persistentDataKey, PersistentDataType.STRING);
    return expectedValue.equals(value);
  }

  private static void setPersistentData(ItemMeta meta, String key)
  {
    PersistentDataContainer dataContainer=meta.getPersistentDataContainer();
    dataContainer.set(persistentDataKey, PersistentDataType.STRING, key);
  }
}
