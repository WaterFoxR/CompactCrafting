package dev.compactmods.crafting.datagen;

import java.util.function.Consumer;
import dev.compactmods.crafting.core.CCItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

public class RecipeGenerator extends RecipeProvider {
    public RecipeGenerator(DataGenerator gen) {
        super(gen.getPackOutput());
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CCItems.FIELD_PROJECTOR_ITEM.get(), 1)
                .requires(CCItems.BASE_ITEM.get())
                .requires(CCItems.PROJECTOR_DISH_ITEM.get())
                .unlockedBy("got_ender_eye", has(Items.ENDER_EYE))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,CCItems.BASE_ITEM.get(), 4)
                .pattern(" R ")
                .pattern("DSD")
                .pattern("PPP")
                .define('S', Items.STONE_SLAB)
                .define('R', Items.REDSTONE_TORCH)
                .define('D', Items.DIAMOND)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("got_ender_eye", has(Items.ENDER_EYE))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,CCItems.PROJECTOR_DISH_ITEM.get(), 4)
                .pattern("GI ")
                .pattern("GEI")
                .pattern("GI ")
                .define('E', Items.ENDER_EYE)
                .define('G', Tags.Items.GLASS_PANES)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("got_ender_eye", has(Items.ENDER_EYE))
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CCItems.MATCH_PROXY_ITEM.get(), 1)
                .requires(CCItems.BASE_ITEM.get())
                .requires(Items.REDSTONE)
                .unlockedBy("got_redstone", has(Items.REDSTONE))
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CCItems.RESCAN_PROXY_ITEM.get(), 1)
                .requires(CCItems.BASE_ITEM.get())
                .requires(Items.CRAFTING_TABLE)
                .unlockedBy("got_crafting_table", has(Items.CRAFTING_TABLE))
                .save(consumer);
    }
}
