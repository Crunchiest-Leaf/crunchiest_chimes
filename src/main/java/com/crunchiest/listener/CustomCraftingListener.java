/**
 * @author Crunchiest-Leaf
 */
package com.crunchiest.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import com.crunchiest.recipes.listenerRecipes.CustomRecipe;
import com.crunchiest.recipes.listenerRecipes.CustomRecipeRegistry;

/**
 * Handles PDC-based custom crafting that is resilient to metadata-modifying plugins.
 */
public class CustomCraftingListener implements Listener
{
  /**
   * Updates crafting result when matrix matches a custom recipe.
   *
   * @param event craft prepare event
   */
  @EventHandler
  public void onCraftPrepare(PrepareItemCraftEvent event)
  {
    CraftingInventory inventory=event.getInventory();
    ItemStack[] matrix=inventory.getMatrix();

    CustomRecipe recipe=CustomRecipeRegistry.findMatchingRecipe(matrix);
    if (recipe != null)
    {
      inventory.setResult(recipe.getResult());
    }
  }

  /**
   * Consumes matrix and gives result for custom recipes from the output slot.
   *
   * @param event click event
   */
  @EventHandler(priority=EventPriority.LOWEST)
  public void onInventoryClick(InventoryClickEvent event)
  {
    if (!(event.getInventory() instanceof CraftingInventory))
    {
      return;
    }

    if (event.getSlot() != 0)
    {
      return;
    }

    CraftingInventory inventory=(CraftingInventory) event.getInventory();
    ItemStack[] matrix=inventory.getMatrix();
    CustomRecipe recipe=CustomRecipeRegistry.findMatchingRecipe(matrix);
    if (recipe == null)
    {
      return;
    }

    event.setCancelled(true);
    ItemStack result=recipe.getResult();
    if (result != null)
    {
      var leftover=event.getWhoClicked().getInventory().addItem(result.clone());
      if (!leftover.isEmpty())
      {
        for (ItemStack drop : leftover.values())
        {
          event.getWhoClicked().getWorld().dropItemNaturally(event.getWhoClicked().getLocation(), drop);
        }
      }
    }

    for (int i=0; i < matrix.length; i++)
    {
      ItemStack item=matrix[i];
      if (item == null || item.getAmount() <= 0)
      {
        continue;
      }

      item.setAmount(item.getAmount() - 1);
      matrix[i]=item.getAmount() > 0 ? item : null;
    }

    inventory.setMatrix(matrix);
    inventory.setResult(CustomRecipeRegistry.findMatchingRecipe(matrix) != null ? recipe.getResult() : null);
  }
}
