package com.crunchiest.recipes.listenerRecipes;

import org.bukkit.inventory.ItemStack;

/**
 * Interface for custom recipes that match by matrix contents and PDC tags.
 */
public interface CustomRecipe
{
  /**
   * Checks whether a matrix matches the recipe.
   *
   * @param matrix 3x3 matrix
   * @return true when matrix matches
   */
  boolean matches(ItemStack[] matrix);

  /**
   * Creates the crafting result.
   *
   * @return result item
   */
  ItemStack getResult();

  /**
   * Returns recipe identifier.
   *
   * @return recipe name
   */
  String getName();
}
