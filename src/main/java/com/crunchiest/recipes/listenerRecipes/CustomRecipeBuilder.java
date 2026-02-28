/**
 * @author Crunchiest-Leaf
 */
package com.crunchiest.recipes.listenerRecipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.crunchiest.CrunchiestChimes;

/**
 * Builder for shaped/shapeless custom recipes with material and PDC matching.
 */
public class CustomRecipeBuilder
{
  private static NamespacedKey pdcKey;

  private final String name;
  private Supplier<ItemStack> resultSupplier;
  private RecipePattern pattern;

  /**
   * Initializes static dependencies.
   *
   * @param plugin plugin instance
   */
  public static void initialize(CrunchiestChimes plugin)
  {
    pdcKey=plugin.getPluginDefaultKey();
  }

  /**
   * Creates a builder.
   *
   * @param name recipe name
   */
  public CustomRecipeBuilder(String name)
  {
    this.name=name;
  }

  /**
   * Sets result supplier.
   *
   * @param resultSupplier supplier for result item
   * @return this builder
   */
  public CustomRecipeBuilder result(Supplier<ItemStack> resultSupplier)
  {
    this.resultSupplier=resultSupplier;
    return this;
  }

  /**
   * Starts a shaped pattern.
   *
   * @param pattern pattern rows
   * @return shaped pattern builder
   */
  public ShapedPatternBuilder shaped(String... pattern)
  {
    return new ShapedPatternBuilder(this, pattern);
  }

  /**
   * Starts a shapeless pattern.
   *
   * @return shapeless pattern builder
   */
  public ShapelessPatternBuilder shapeless()
  {
    return new ShapelessPatternBuilder(this);
  }

  /**
   * Builds the recipe.
   *
   * @return built recipe
   */
  public CustomRecipe build()
  {
    if (pattern == null)
    {
      throw new IllegalStateException("Recipe pattern must be set");
    }
    if (resultSupplier == null)
    {
      throw new IllegalStateException("Recipe result must be set");
    }

    return new CustomRecipeImpl(name, pattern, resultSupplier);
  }

  void setPattern(RecipePattern pattern)
  {
    this.pattern=pattern;
  }

  interface RecipePattern
  {
    boolean matches(ItemStack[] matrix);
  }

  private static class CustomRecipeImpl implements CustomRecipe
  {
    private final String name;
    private final RecipePattern pattern;
    private final Supplier<ItemStack> resultSupplier;

    private CustomRecipeImpl(String name, RecipePattern pattern, Supplier<ItemStack> resultSupplier)
    {
      this.name=name;
      this.pattern=pattern;
      this.resultSupplier=resultSupplier;
    }

    @Override
    public boolean matches(ItemStack[] matrix)
    {
      return pattern.matches(matrix);
    }

    @Override
    public ItemStack getResult()
    {
      return resultSupplier.get();
    }

    @Override
    public String getName()
    {
      return name;
    }
  }

  /**
   * Builder for shaped recipe patterns.
   */
  public static class ShapedPatternBuilder
  {
    private final CustomRecipeBuilder parent;
    private final char[][] pattern;
    private final Map<Character, IngredientMatcher> ingredients;

    private ShapedPatternBuilder(CustomRecipeBuilder parent, String[] patternLines)
    {
      this.parent=parent;
      this.pattern=new char[3][3];
      this.ingredients=new HashMap<>();

      for (int row=0; row < 3; row++)
      {
        String line=row < patternLines.length ? patternLines[row] : "   ";
        for (int col=0; col < 3; col++)
        {
          this.pattern[row][col]=col < line.length() ? line.charAt(col) : ' ';
        }
      }
    }

    public ShapedPatternBuilder ingredient(char character, Material material)
    {
      ingredients.put(character, new MaterialMatcher(material));
      return this;
    }

    public ShapedPatternBuilder ingredient(char character, Material... materials)
    {
      ingredients.put(character, new MaterialMatcher(materials));
      return this;
    }

    public ShapedPatternBuilder pdcIngredient(char character, String pdcValue)
    {
      ingredients.put(character, new PdcMatcher(pdcValue));
      return this;
    }

    public CustomRecipeBuilder build()
    {
      parent.setPattern(new ShapedPattern(pattern, ingredients));
      return parent;
    }
  }

  /**
   * Builder for shapeless recipe patterns.
   */
  public static class ShapelessPatternBuilder
  {
    private final CustomRecipeBuilder parent;
    private final List<IngredientMatcher> ingredients;

    private ShapelessPatternBuilder(CustomRecipeBuilder parent)
    {
      this.parent=parent;
      this.ingredients=new ArrayList<>();
    }

    public ShapelessPatternBuilder ingredient(Material material)
    {
      ingredients.add(new MaterialMatcher(material));
      return this;
    }

    public ShapelessPatternBuilder ingredient(Material... materials)
    {
      ingredients.add(new MaterialMatcher(materials));
      return this;
    }

    public ShapelessPatternBuilder pdcIngredient(String pdcValue)
    {
      ingredients.add(new PdcMatcher(pdcValue));
      return this;
    }

    public CustomRecipeBuilder build()
    {
      parent.setPattern(new ShapelessPattern(ingredients));
      return parent;
    }
  }

  interface IngredientMatcher
  {
    boolean matches(ItemStack item);
  }

  private static class MaterialMatcher implements IngredientMatcher
  {
    private final Set<Material> materials;

    private MaterialMatcher(Material... materials)
    {
      this.materials=new HashSet<>(Arrays.asList(materials));
    }

    @Override
    public boolean matches(ItemStack item)
    {
      return item != null && materials.contains(item.getType());
    }
  }

  private static class PdcMatcher implements IngredientMatcher
  {
    private final String pdcValue;

    private PdcMatcher(String pdcValue)
    {
      this.pdcValue=pdcValue;
    }

    @Override
    public boolean matches(ItemStack item)
    {
      if (item == null || !item.hasItemMeta() || pdcKey == null)
      {
        return false;
      }

      ItemMeta meta=item.getItemMeta();
      if (meta == null)
      {
        return false;
      }

      PersistentDataContainer pdc=meta.getPersistentDataContainer();
      if (!pdc.has(pdcKey, PersistentDataType.STRING))
      {
        return false;
      }

      String value=pdc.get(pdcKey, PersistentDataType.STRING);
      return pdcValue.equals(value);
    }
  }

  private static class ShapedPattern implements RecipePattern
  {
    private final char[][] pattern;
    private final Map<Character, IngredientMatcher> ingredients;

    private ShapedPattern(char[][] pattern, Map<Character, IngredientMatcher> ingredients)
    {
      this.pattern=pattern;
      this.ingredients=ingredients;
    }

    @Override
    public boolean matches(ItemStack[] matrix)
    {
      if (matrix == null || matrix.length != 9)
      {
        return false;
      }

      for (int row=0; row < 3; row++)
      {
        for (int col=0; col < 3; col++)
        {
          int index=row * 3 + col;
          char patternChar=pattern[row][col];
          ItemStack item=matrix[index];

          if (patternChar == ' ')
          {
            if (item != null)
            {
              return false;
            }
          }
          else
          {
            IngredientMatcher matcher=ingredients.get(patternChar);
            if (matcher == null || !matcher.matches(item))
            {
              return false;
            }
          }
        }
      }

      return true;
    }
  }

  private static class ShapelessPattern implements RecipePattern
  {
    private final List<IngredientMatcher> ingredients;

    private ShapelessPattern(List<IngredientMatcher> ingredients)
    {
      this.ingredients=ingredients;
    }

    @Override
    public boolean matches(ItemStack[] matrix)
    {
      List<ItemStack> items=Arrays.stream(matrix).filter(item -> item != null).toList();
      if (items.size() != ingredients.size())
      {
        return false;
      }

      List<IngredientMatcher> remaining=new ArrayList<>(ingredients);
      for (ItemStack item : items)
      {
        boolean found=false;
        for (int i=0; i < remaining.size(); i++)
        {
          if (remaining.get(i).matches(item))
          {
            remaining.remove(i);
            found=true;
            break;
          }
        }

        if (!found)
        {
          return false;
        }
      }

      return remaining.isEmpty();
    }
  }
}
