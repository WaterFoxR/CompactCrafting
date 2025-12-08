package dev.compactmods.crafting.integration.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public enum CCLayerType {
    EMPTY_WIP("compactcrafting:empty"),
    HOLLOW_WIP("compactcrafting:hollow"),
    FILLED_WIP("compactcrafting:filled"),
    MIXED("compactcrafting:mixed");

    public final String string;

    CCLayerType(String string) {
        this.string = string;
    }

    public JsonObject withPattern(String[][] pattern) {
        JsonObject p = new JsonObject();
        p.addProperty("type",this.string);
        JsonArray patternJson = new JsonArray();
        for(String[] row : pattern){
            var rowJson = new JsonArray();
            for(String cell : row){
                rowJson.add(cell);
            }
            patternJson.add(rowJson);
        }
        p.add("pattern", patternJson);
        return p;
    }
}
