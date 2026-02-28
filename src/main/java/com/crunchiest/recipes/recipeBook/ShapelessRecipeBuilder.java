package com.crunchiest.recipes.recipeBook;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;

/**
 * Builder for Bukkit shapeless recipes.
 */
public class ShapelessRecipeBuilder
{
  private final ShapelessRecipe recipe;

  /**
   * Creates a builder.
   *
   * @param key recipe key
   * @param result result item
   */
  public ShapelessRecipeBuilder(NamespacedKey key, ItemStack result)
  {
    this.recipe=new ShapelessRecipe(key, result);
  }

  public ShapelessRecipeBuilder addIngredient(Material material)
  {
    recipe.addIngredient(material);
    return this;
  }

  public ShapelessRecipeBuilder addIngredient(RecipeChoice choice)
  {
    recipe.addIngredient(choice);
    return this;
  }

  public ShapelessRecipe build()
  {
    return recipe;
  }
}
