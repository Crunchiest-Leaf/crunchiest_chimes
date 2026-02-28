package com.crunchiest.recipes.recipeBook;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import com.crunchiest.CrunchiestChimes;
import com.crunchiest.items.ItemFactory;

/**
 * Optional Bukkit recipe-book registration helpers.
 */
public final class RecipeRegistrar
{
  private RecipeRegistrar()
  {
  }

  /**
   * Registers plugin recipe-book recipes.
   *
   * @param plugin plugin instance
   */
  public static void registerAllRecipes(CrunchiestChimes plugin)
  {
    registerCustomNoteBlockRecipe(plugin);
  }

  /**
   * Unregisters plugin recipe-book recipes.
   *
   * @param plugin plugin instance
   */
  public static void unregisterAllRecipes(CrunchiestChimes plugin)
  {
    NamespacedKey[] keys={new NamespacedKey(plugin, "cc_custom_note_block")};

    for (NamespacedKey key : keys)
    {
      Iterator<Recipe> iterator=Bukkit.getServer().recipeIterator();
      while (iterator.hasNext())
      {
        Recipe recipe=iterator.next();
        if (recipe instanceof ShapedRecipe && key.equals(((ShapedRecipe) recipe).getKey()))
        {
          iterator.remove();
        }
        if (recipe instanceof ShapelessRecipe && key.equals(((ShapelessRecipe) recipe).getKey()))
        {
          iterator.remove();
        }
      }
    }
  }

  /**
   * Returns registered recipe keys.
   *
   * @param plugin plugin instance
   * @return recipe keys
   */
  public static List<NamespacedKey> getRegisteredRecipes(CrunchiestChimes plugin)
  {
    return List.of(new NamespacedKey(plugin, "cc_custom_note_block"));
  }

  private static void registerCustomNoteBlockRecipe(CrunchiestChimes plugin)
  {
    NamespacedKey key=new NamespacedKey(plugin, "cc_custom_note_block");
    if (Bukkit.getRecipe(key) != null)
    {
      return;
    }

    ShapedRecipe recipe=new ShapedRecipeBuilder(key, ItemFactory.createCustomNoteBlock())
      .shape("ARA", "RNR", "ARA")
      .setIngredient('A', Material.AMETHYST_SHARD)
      .setIngredient('R', Material.REDSTONE)
      .setIngredient('N', Material.NOTE_BLOCK)
      .build();

    Bukkit.addRecipe(recipe);
  }
}
