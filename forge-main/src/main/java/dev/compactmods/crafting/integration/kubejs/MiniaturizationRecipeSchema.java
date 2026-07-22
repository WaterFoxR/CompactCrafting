package dev.compactmods.crafting.integration.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

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

        /**
         * Override deserialize to handle CompactCrafting's item format ({id, Count, tag/nbt})
         * instead of KubeJS's format ({item, count, nbt}).
         * Temporarily converts CC format fields to KubeJS format before calling super,
         * then restores the original CC format JSON for the vanilla recipe parser.
         */
        @Override
        public void deserialize(boolean merge) {
            // Save original fields
            JsonElement originalCatalyst = json.get("catalyst");
            JsonElement originalOutputs = json.get("outputs");

            boolean convertedCatalyst = false;
            boolean convertedOutputs = false;

            // Convert catalyst from CC format to KubeJS format if needed
            if (originalCatalyst != null && originalCatalyst.isJsonObject()) {
                var catObj = originalCatalyst.getAsJsonObject();
                if (catObj.has("id")) {
                    convertedCatalyst = true;
                    JsonObject kjsCatalyst = ccItemToKjsItem(catObj);
                    json.add("catalyst", kjsCatalyst);
                }
            }

            // Convert outputs from CC format to KubeJS format if needed
            if (originalOutputs != null && originalOutputs.isJsonArray()) {
                var outArr = originalOutputs.getAsJsonArray();
                if (outArr.size() > 0 && outArr.get(0).isJsonObject() && outArr.get(0).getAsJsonObject().has("id")) {
                    convertedOutputs = true;
                    JsonArray kjsOutputs = new JsonArray();
                    for (int i = 0; i < outArr.size(); i++) {
                        var outObj = outArr.get(i).getAsJsonObject();
                        kjsOutputs.add(ccItemToKjsItem(outObj));
                    }
                    json.add("outputs", kjsOutputs);
                }
            }

            try {
                super.deserialize(merge);
            } finally {
                // Restore original CC format for the vanilla recipe parser
                if (convertedCatalyst) {
                    json.add("catalyst", originalCatalyst);
                }
                if (convertedOutputs) {
                    json.add("outputs", originalOutputs);
                }
            }
        }

        /**
         * Converts a CompactCrafting-format item ({id, Count, tag/nbt}) to KubeJS-format ({item, count, nbt}).
         */
        private JsonObject ccItemToKjsItem(JsonObject ccItem) {
            JsonObject kjsItem = new JsonObject();
            kjsItem.addProperty("item", ccItem.get("id").getAsString());

            if (ccItem.has("Count")) {
                int count = ccItem.get("Count").getAsInt();
                if (count > 1) {
                    kjsItem.addProperty("count", count);
                }
            }

            // Copy NBT data (CC uses "tag" or "nbt", KubeJS uses "nbt")
            if (ccItem.has("tag")) {
                kjsItem.add("nbt", ccItem.get("tag"));
            } else if (ccItem.has("nbt")) {
                kjsItem.add("nbt", ccItem.get("nbt"));
            }

            return kjsItem;
        }

        @Override
        public void serialize() {
            super.serialize();

            // Convert catalyst from KubeJS format to CC format
            if (json.has("catalyst")) {
                json.remove("catalyst");
                json.add("catalyst", inputItemToCCJson(CATALYST));
            }

            // Convert outputs from KubeJS format to CC format
            if (json.has("outputs")) {
                json.remove("outputs");
                json.add("outputs", outputItemsToCCJson(OUTPUTS));
            }
        }

        /**
         * Converts a KubeJS InputItem to CompactCrafting's item format ({id, Count, tag}).
         */
        public JsonObject inputItemToCCJson(RecipeKey<InputItem> key) {
            InputItem inputItem = this.getValue(key);
            int count = inputItem.count;

            // Get the first matching item stack from the ingredient
            ItemStack[] stacks = inputItem.ingredient.getItems();
            ItemStack first = stacks.length > 0 ? stacks[0] : ItemStack.EMPTY;
            String id = ForgeRegistries.ITEMS.getKey(first.getItem()).toString();

            JsonObject ccItemJson = new JsonObject();
            ccItemJson.addProperty("id", id);
            ccItemJson.addProperty("Count", count);

            if (first.hasTag()) {
                ccItemJson.addProperty("tag", first.getTag().toString());
            }

            return ccItemJson;
        }

        /**
         * Converts KubeJS OutputItems to CompactCrafting's item format ([{id, Count, tag}]).
         */
        public JsonArray outputItemsToCCJson(RecipeKey<OutputItem[]> key) {
            OutputItem[] outputItems = this.getValue(key);
            JsonArray ccOutputItemsJson = new JsonArray(outputItems.length);

            for (OutputItem outputItem : outputItems) {
                String id = ForgeRegistries.ITEMS.getKey(outputItem.item.getItem()).toString();
                int count = outputItem.getCount();

                JsonObject ccItemJson = new JsonObject();
                ccItemJson.addProperty("id", id);
                ccItemJson.addProperty("Count", count);

                if (outputItem.item.hasTag()) {
                    ccItemJson.addProperty("tag", outputItem.item.getTag().toString());
                }

                ccOutputItemsJson.add(ccItemJson);
            }

            return ccOutputItemsJson;
        }

        /**
         * 设置配方占地区域大小。
         * 使用 FILLED / HOLLOW / EMPTY 层时必须设置此项。
         */
        @SuppressWarnings("unused")
        public CompactCraftingJS setRecipeSize(int size) {
            this.json.addProperty("recipeSize", size);
            save();
            return this;
        }

        /**
         * 设置合成时间（tick），默认 200（10秒）。
         */
        @SuppressWarnings("unused")
        public CompactCraftingJS setCraftingTime(int time) {
            this.json.addProperty("craftingTime", time);
            save();
            return this;
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