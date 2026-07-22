package dev.compactmods.crafting.integration.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * KubeJS 层类型辅助类，用于在 KubeJS 脚本中创建 Compact Crafting 配方层。
 *
 * 使用示例：
 * <pre>
 * // MIXED 层（混合模式 - 通过网格指定每个方块）
 * CCLayerType.MIXED.withPattern([
 *     ["A", "A", "A"],
 *     ["A", "-", "A"],
 *     ["A", "A", "A"]
 * ])
 *
 * // FILLED 层（整层填满一个方块）
 * CCLayerType.FILLED.withComponent("G")
 *
 * // HOLLOW 层（外层墙壁）
 * CCLayerType.HOLLOW.withWall("G")
 *
 * // EMPTY 层（全空）
 * CCLayerType.EMPTY.build()
 * </pre>
 */
public enum CCLayerType {
    MIXED("compactcrafting:mixed"),
    FILLED("compactcrafting:filled"),
    HOLLOW("compactcrafting:hollow"),
    EMPTY("compactcrafting:empty");

    public final String typeName;

    CCLayerType(String typeName) {
        this.typeName = typeName;
    }

    /**
     * 创建 MIXED 层（混合模式）。
     * 通过二维字符串数组定义每个位置的方块组件。
     * "-" 表示该位置为空。
     */
    public JsonObject withPattern(String[][] pattern) {
        JsonObject p = new JsonObject();
        p.addProperty("type", this.typeName);
        JsonArray patternJson = new JsonArray();
        for(String[] row : pattern){
            JsonArray rowJson = new JsonArray();
            for(String cell : row){
                rowJson.add(cell);
            }
            patternJson.add(rowJson);
        }
        p.add("pattern", patternJson);
        return p;
    }

    /**
     * 创建 FILLED 层（填充模式）。
     * 整个层都用指定的组件填满。
     * 需要配方指定 recipeSize。
     */
    public JsonObject withComponent(String component) {
        JsonObject p = new JsonObject();
        p.addProperty("type", this.typeName);
        p.addProperty("component", component);
        return p;
    }

    /**
     * 创建 HOLLOW 层（空心模式）。
     * 外层墙壁使用指定组件，内部为空。
     * 需要配方指定 recipeSize。
     */
    public JsonObject withWall(String wall) {
        JsonObject p = new JsonObject();
        p.addProperty("type", this.typeName);
        p.addProperty("wall", wall);
        return p;
    }

    /**
     * 创建 EMPTY 层（空层）。
     * 整个层均为空。
     * 需要配方指定 recipeSize。
     */
    public JsonObject build() {
        JsonObject p = new JsonObject();
        p.addProperty("type", this.typeName);
        return p;
    }
}
