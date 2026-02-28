package com.crunchiest.recipes.listenerRecipes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.crunchiest.items.ItemFactory;

/**
 * Registry for all custom listener-driven recipes.
 */
public final class CustomRecipeRegistry
{
  private static final List<CustomRecipe> RECIPES=new ArrayList<>();

  private CustomRecipeRegistry()
  {
  }

  /**
   * Clears all registered custom recipes.
   */
  public static void unregisterAllRecipes()
  {
    RECIPES.clear();
  }

  /**
   * Registers all plugin custom recipes.
   */
  public static void registerAllRecipes()
  {
    unregisterAllRecipes();

    RECIPES.add(
      new CustomRecipeBuilder("custom_note_block")
        .result(ItemFactory::createCustomNoteBlock)
        .shaped("ARA", "RNR", "ARA")
        .ingredient('A', Material.AMETHYST_SHARD)
        .ingredient('R', Material.REDSTONE)
        .ingredient('N', Material.NOTE_BLOCK)
        .build()
        .build()
    );
  }

  /**
   * Returns all registered recipes.
   *
   * @return recipe copy
   */
  public static List<CustomRecipe> getRecipes()
  {
    return new ArrayList<>(RECIPES);
  }

  /**
   * Finds the first recipe matching the matrix.
   *
   * @param matrix crafting matrix
   * @return matched recipe or null
   */
  public static CustomRecipe findMatchingRecipe(ItemStack[] matrix)
  {
    for (CustomRecipe recipe : RECIPES)
    {
      if (recipe.matches(matrix))
      {
        return recipe;
      }
    }
    return null;
  }
}
