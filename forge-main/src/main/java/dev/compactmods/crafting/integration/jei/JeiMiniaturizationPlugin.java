package dev.compactmods.crafting.integration.jei;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.core.CCItems;
import dev.compactmods.crafting.core.CCMiniaturizationRecipes;
import dev.compactmods.crafting.server.ServerConfig;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashSet;
import java.util.Set;

@JeiPlugin
public class JeiMiniaturizationPlugin implements IModPlugin {

    /** 内置示例配方的 ID 列表 */
    private static final Set<ResourceLocation> EXAMPLE_RECIPE_IDS = new HashSet<>(Set.of(
            CompactCrafting.modRL("basic_mixed_medium_iron"),
            CompactCrafting.modRL("compact_walls"),
            CompactCrafting.modRL("ender_crystal"),
            CompactCrafting.modRL("medium_glass_walls_obsidian_center"),
            CompactCrafting.modRL("chicken")
    ));

    /**
     * 判断是否应隐藏某配方（示例配方且配置关闭时隐藏）。
     */
    public static boolean shouldHideRecipe(ResourceLocation recipeId) {
        if (!EXAMPLE_RECIPE_IDS.contains(recipeId)) return false;
        return !ServerConfig.LOAD_EXAMPLE_RECIPES.get();
    }

    @Override
    public ResourceLocation getPluginUid() {
        return CompactCrafting.modRL("miniaturization_crafting");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new JeiMiniaturizationCraftingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(CCItems.FIELD_PROJECTOR_ITEM.get(), 4),
                JeiMiniaturizationCraftingCategory.RECIPE_TYPE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager rm = null;

        ClientLevel clientLevel = Minecraft.getInstance().level;
        if (clientLevel != null) {
            rm = clientLevel.getRecipeManager();
        }

        if (rm == null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                rm = server.getRecipeManager();
            }
        }

        if (rm == null) {
            CompactCrafting.LOGGER.warn("Could not retrieve RecipeManager for JEI integration.");
            return;
        }

        var miniRecipes = rm.getAllRecipesFor(CCMiniaturizationRecipes.MINIATURIZATION_RECIPE.get())
                .stream()
                .filter(r -> {
                    if (EXAMPLE_RECIPE_IDS.contains(r.getId()))
                        return ServerConfig.LOAD_EXAMPLE_RECIPES.get();
                    return true;
                })
                .toList();

        if (miniRecipes.isEmpty()) {
            CompactCrafting.LOGGER.info("No miniaturization recipes found for JEI integration.");
        } else {
            CompactCrafting.LOGGER.info("Registering {} miniaturization recipes with JEI.", miniRecipes.size());
        }

        registration.addRecipes(JeiMiniaturizationCraftingCategory.RECIPE_TYPE, miniRecipes);
    }
}
