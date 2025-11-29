package dev.compactmods.crafting.recipes.setup;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public abstract class RecipeBase implements Recipe<FakeInventory> {

    /**
     * Used to check if a recipe matches current crafting inventory
     *
     * @param inv The inventory to check.
     * @param worldIn The world to check in.
     */
    @Override
    public boolean matches(FakeInventory inv, Level worldIn) {
        return true;
    }


    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    /**
     * Get the result of this recipe, usually for display purposes (e.g. recipe book). If your recipe has more than one
     * possible result (e.g. it's dynamic and depends on its inputs), then return an empty stack.
     */
    @Override
    public boolean isSpecial() {
        return true;
    }

    public abstract void setId(ResourceLocation recipeId);

}
