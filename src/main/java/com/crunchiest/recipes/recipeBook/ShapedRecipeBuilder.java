/**
 * @author Crunchiest-Leaf
 */
package com.crunchiest.recipes.recipeBook;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

/**
 * Builder for Bukkit shaped recipes.
 */
public class ShapedRecipeBuilder
{
  private final ShapedRecipe recipe;

  /**
   * Creates a builder.
   *
   * @param key recipe key
   * @param result result item
   */
  public ShapedRecipeBuilder(NamespacedKey key, ItemStack result)
  {
    this.recipe=new ShapedRecipe(key, result);
  }

  public ShapedRecipeBuilder shape(String... shape)
  {
    recipe.shape(shape);
    return this;
  }

  public ShapedRecipeBuilder setIngredient(char key, Material material)
  {
    recipe.setIngredient(key, material);
    return this;
  }

  public ShapedRecipeBuilder setIngredient(char key, RecipeChoice choice)
  {
    recipe.setIngredient(key, choice);
    return this;
  }

  public ShapedRecipe build()
  {
    return recipe;
  }
}
