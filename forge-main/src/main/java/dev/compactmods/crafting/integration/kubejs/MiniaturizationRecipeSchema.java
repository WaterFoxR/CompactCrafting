package dev.compactmods.crafting.integration.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.compactmods.crafting.CompactCrafting;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.client.Minecraft;

public interface MiniaturizationRecipeSchema {
    // 必须字段
    RecipeKey<OutputItem[]> OUTPUTS = ItemComponents.OUTPUT_ARRAY.key("outputs");
    RecipeKey<InputItem> CATALYST = ItemComponents.INPUT.key("catalyst");

//    RecipeKey<String> LAYERS = StringComponent.NON_EMPTY.key("layers");
//    RecipeKey<String> COMPONENTS = StringComponent.NON_EMPTY.key("components");

    // 可选字段
    RecipeKey<Integer> CRAFTING_TIME = NumberComponent.INT.key("craftingTime").optional(200);
    RecipeKey<Integer> RECIPE_SIZE = NumberComponent.INT.key("recipeSize").optional(-1);

    class CompactCraftingJS extends RecipeJS {
        public final String COMPONENT_TYPE = "compactcrafting:block";

        public CompactCraftingJS setLayers(JsonObject[] layerWithPattern){
            this.json.remove("layers");
            JsonArray layersJson = new JsonArray();
            for(JsonObject layer : layerWithPattern){
                layersJson.add(layer);
            }
            this.json.add("layers", layersJson);
            return this;
        }

        public CompactCraftingJS setComponents(JsonObject BlockComponent){
            this.json.remove("components");
            this.json.add("components", BlockComponent);
            return this;
        }


        @Override
        public void serialize() {
            super.serialize();
            this.json.remove("catalyst");
            this.json.add("catalyst", InputItemToCCJson(CATALYST));

            this.json.remove("outputs");
            this.json.add("outputs", OutputItemsToCCJson(OUTPUTS));
        }

        public JsonObject InputItemToCCJson(RecipeKey<InputItem> key) {
            InputItem inputItem = this.getValue(key);
            JsonObject itemObject = inputItem.toJsonJS().getAsJsonObject();

            String id = itemObject.get("ingredient").getAsJsonObject().get("item").getAsString();
            int count = inputItem.count;

            JsonObject CCItemJson = new JsonObject();
            CCItemJson.addProperty("id", id);
            CCItemJson.addProperty("Count", count);

            return CCItemJson;
        }

        public JsonArray OutputItemsToCCJson(RecipeKey<OutputItem[]> key){
            OutputItem[] outputItems = this.getValue(key);
            JsonArray CCOutputItemsJson = new JsonArray(outputItems.length);
            for(OutputItem outputItem : outputItems){

                String id = RegistryInfo.ITEM.getId(outputItem.item.getItem()).toString();
                int count = outputItem.getCount();

                JsonObject CCItemJson = new JsonObject();
                CCItemJson.addProperty("id", id);
                CCItemJson.addProperty("Count", count);

                CCOutputItemsJson.add(CCItemJson);
            }
            return CCOutputItemsJson;
        }


        @Override
        public void afterLoaded() {
            if(Minecraft.getInstance().options.advancedItemTooltips) {
                CompactCrafting.ClientPlayerTell("显示所有值:" + this.getAllValueMap());
                CompactCrafting.ClientPlayerTell("显示原始Json:" + this.originalJson);
                CompactCrafting.ClientPlayerTell("显示Json:" + this.json);
            }
            super.afterLoaded();
        }
    }

    RecipeSchema SCHEMA = new RecipeSchema(
            CompactCraftingJS.class,
            CompactCraftingJS::new,
            OUTPUTS,
            CATALYST,
//            LAYERS,
//            COMPONENTS,
            CRAFTING_TIME,
            RECIPE_SIZE
            );

}