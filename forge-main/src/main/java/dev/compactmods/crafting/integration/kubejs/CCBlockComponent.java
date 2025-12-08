package dev.compactmods.crafting.integration.kubejs;

import com.google.gson.JsonObject;

public class CCBlockComponent {

    public JsonObject components = new JsonObject();

    public final String COMPONENT_TYPE = "compactcrafting:block";
    
    public CCBlockComponent add(String key, String blockId) {
        JsonObject c = new JsonObject();
        JsonObject tag = new JsonObject();

        tag.addProperty("type", COMPONENT_TYPE);
        tag.addProperty("block", blockId);

        components.add(key, tag);
        return this;
    }

    public JsonObject build() {
        return components;
    }
}
