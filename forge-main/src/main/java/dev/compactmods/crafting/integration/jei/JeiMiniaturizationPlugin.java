package dev.compactmods.crafting.integration.jei;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.core.CCItems;
import dev.compactmods.crafting.core.CCMiniaturizationRecipes;
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

@JeiPlugin
public class JeiMiniaturizationPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return CompactCrafting.modRL("miniaturization_crafting");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new JeiMiniaturizationCraftingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

//    @Override
//    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
//    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(CCItems.FIELD_PROJECTOR_ITEM.get(), 4),
                JeiMiniaturizationCraftingCategory.RECIPE_TYPE);

    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
//        ClientLevel w = Minecraft.getInstance().level;
//        RecipeManager rm = w == null ? null : w.getRecipeManager();
//        if(rm != null) {
//            final var miniRecipes = rm.getAllRecipesFor(CCMiniaturizationRecipes.MINIATURIZATION_RECIPE.get());
//            registration.addRecipes(JeiMiniaturizationCraftingCategory.RECIPE_TYPE, miniRecipes);
//        }
        RecipeManager rm = null;

        // 首先尝试从客户端世界获取配方管理器
        ClientLevel clientLevel = Minecraft.getInstance().level;
        if (clientLevel != null) {
            rm = clientLevel.getRecipeManager();
        }

        // 如果客户端世界为null，尝试从服务器获取
        if (rm == null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                rm = server.getRecipeManager();
            }
        }

        // 如果仍然无法获取配方管理器，记录错误并返回
        if (rm == null) {
            CompactCrafting.LOGGER.warn("Could not retrieve RecipeManager for JEI integration. Miniaturization recipes will not be visible in JEI.");
            return;
        }

        // 获取并注册配方
        final var miniRecipes = rm.getAllRecipesFor(CCMiniaturizationRecipes.MINIATURIZATION_RECIPE.get());
        if (miniRecipes.isEmpty()) {
            CompactCrafting.LOGGER.info("No miniaturization recipes found for JEI integration.");
        } else {
            CompactCrafting.LOGGER.info("Registering {} miniaturization recipes with JEI.", miniRecipes.size());
        }

        registration.addRecipes(JeiMiniaturizationCraftingCategory.RECIPE_TYPE, miniRecipes);
    }
}
