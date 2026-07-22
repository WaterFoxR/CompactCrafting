// CompactCrafting KubeJS 配方示例
// 支持的层类型：
//   CCLayerType.MIXED     - 混合模式，通过网格精确定义每格
//   CCLayerType.FILLED    - 填充模式，整层填满一个方块
//   CCLayerType.HOLLOW    - 空心模式，外层墙壁 + 内部空心
//   CCLayerType.EMPTY     - 空层

// 辅助工具：
//   CCBlockComponent      - 定义配方中用到的方块组件
//   Item.of(id, count)    - KubeJS 原生物品定义

ServerEvents.recipes(event => {

    // =========================================================
    // 示例 1: 纯 MIXED 层 - 鸡配方
    // 多层混合模式模拟鸡的3D形状
    // =========================================================
    event.recipes.compactcrafting
        .miniaturization(
            Item.of('minecraft:chicken_spawn_egg', 1),        // 输出
            Item.of('minecraft:egg', 1)                        // 催化剂
        ).setLayers([
            CCLayerType.MIXED.withPattern([
                ["WW", "WW", "WW", "WW", "WW"],
                ["WW", "WW", "WW", "WW", "WW"],
                ["WW", "WW", "WW", "WW", "WW"],
                ["-", "-", "-", "-", "-"],
                ["-", "-", "-", "-", "-"]
            ]),
            CCLayerType.MIXED.withPattern([
                ["WW", "WW", "WW", "WW", "WW"],
                ["WW", "WW", "WW", "WW", "WW"],
                ["BW", "WW", "WW", "WW", "BW"],
                ["-", "-", "-", "-", "-"],
                ["-", "-", "-", "-", "-"]
            ]),
            CCLayerType.MIXED.withPattern([
                ["WW", "WW", "WW", "WW", "WW"],
                ["WW", "WW", "WW", "WW", "WW"],
                ["WW", "WW", "WW", "WW", "WW"],
                ["-", "OW", "OW", "OW", "-"],
                ["-", "OW", "OW", "OW", "-"]
            ]),
            CCLayerType.MIXED.withPattern([
                ["WW", "WW", "WW", "WW", "WW"],
                ["WW", "WW", "WW", "WW", "WW"],
                ["WW", "WW", "WW", "WW", "WW"],
                ["-", "-", "RW", "-", "-"],
                ["-", "-", "-", "-", "-"]
            ]),
            CCLayerType.MIXED.withPattern([
                ["WW", "WW", "WW", "WW", "WW"],
                ["WW", "WW", "WW", "WW", "WW"],
                ["WW", "WW", "WW", "WW", "WW"],
                ["-", "-", "RW", "-", "-"],
                ["-", "-", "-", "-", "-"]
            ])
        ]).setComponents(new CCBlockComponent()
            .add("WW", "minecraft:white_wool")
            .add("OW", "minecraft:orange_wool")
            .add("RW", "minecraft:red_wool")
            .add("BW", "minecraft:black_wool")
            .build()
        )

    // =========================================================
    // 示例 2: 纯 FILLED 层 - 压缩墙壁
    // recipeSize: 1 表示每个 FILLED 层占 1x1 区域
    // 两层叠加：下层红石线，上层铁块
    // =========================================================
    event.recipes.compactcrafting
        .miniaturization(
            Item.of('minecraft:diamond', 1),                  // 输出
            Item.of('minecraft:ender_pearl', 5)                // 催化剂
        ).setLayers([
            CCLayerType.FILLED.withComponent("R"),             // 底层：全部红石线
            CCLayerType.FILLED.withComponent("I")              // 顶层：全部铁块
        ]).setComponents(new CCBlockComponent()
            .add("R", "minecraft:redstone_wire")
            .add("I", "minecraft:iron_block")
            .build()
        ).setRecipeSize(1)                                     // 重要：FILLED/HOLLOW/EMPTY 层需要 recipeSize

    // =========================================================
    // 示例 3: FILLED + HOLLOW + MIXED 混合 - 末影水晶
    // =========================================================
    event.recipes.compactcrafting
        .miniaturization(
            [Item.of('minecraft:end_crystal', 4)],             // 输出（数组形式）
            Item.of('minecraft:ender_pearl', 1)                // 催化剂
        ).setLayers([
            CCLayerType.FILLED.withComponent("G"),             // 第1层 (底): 全部玻璃
            CCLayerType.HOLLOW.withWall("G"),                  // 第2层: 玻璃墙壁
            CCLayerType.MIXED.withPattern([                    // 第3层: 玻璃框+黑曜石心
                ["G", "G", "G", "G", "G"],
                ["G", "-", "-", "-", "G"],
                ["G", "-", "O", "-", "G"],
                ["G", "-", "-", "-", "G"],
                ["G", "G", "G", "G", "G"]
            ]),
            CCLayerType.HOLLOW.withWall("G"),                  // 第4层: 玻璃墙壁
            CCLayerType.FILLED.withComponent("G")              // 第5层 (顶): 全部玻璃
        ]).setComponents(new CCBlockComponent()
            .add("G", "minecraft:glass")
            .add("O", "minecraft:obsidian")
            .build()
        ).setRecipeSize(5)                                     // 5x5 区域

    // =========================================================
    // 示例 4: 单层 MIXED + 多输出 + NBT催化剂
    // 注意：catalyst 和 outputs 都支持 NBT
    // =========================================================
    event.recipes.compactcrafting
        .miniaturization(
            [
                Item.of('minecraft:diamond', 1),
                Item.of('minecraft:iron_ingot', 4)
            ],
            Item.of('minecraft:enchanted_book').enchant('minecraft:protection', 4),  // NBT 催化剂
            160,                                             // craftingTime (可选，默认200)
            3                                                // recipeSize (可选，用于动态层)
        ).setLayers([
            CCLayerType.MIXED.withPattern([
                ["I", "G", "G", "G", "G"],
                ["G", "-", "-", "-", "G"],
                ["G", "-", "O", "-", "G"],
                ["G", "-", "-", "-", "G"],
                ["G", "G", "G", "G", "G"]
            ])
        ]).setComponents(new CCBlockComponent()
            .add("G", "minecraft:glass")
            .add("O", "minecraft:obsidian")
            .add("I", "minecraft:iron_block")
            .build()
        )

    // =========================================================
    // 示例 5: 仅 HOLLOW 层 - 空心玻璃箱
    // recipeSize 决定箱子大小（3x3 = 3x3x3 结构）
    // 3层全部 HOLLOW 形成一个空心立方体
    // =========================================================
    event.recipes.compactcrafting
        .miniaturization(
            Item.of('minecraft:glass', 16),
            Item.of('minecraft:glass_pane', 1)
        ).setLayers([
            CCLayerType.FILLED.withComponent("G"),             // 底: 实心玻璃
            CCLayerType.HOLLOW.withWall("G"),                  // 中: 玻璃墙壁（内部空心）
            CCLayerType.FILLED.withComponent("G")              // 顶: 实心玻璃
        ]).setComponents(new CCBlockComponent()
            .add("G", "minecraft:glass")
            .build()
        ).setRecipeSize(3)                                     // 3x3x3 结构

    // =========================================================
    // 示例 6: 含空层的高级配方
    // 使用 EMPTY 层在中间制造空隙
    // =========================================================
    event.recipes.compactcrafting
        .miniaturization(
            Item.of('minecraft:iron_block', 2),
            Item.of('minecraft:iron_ingot', 1)
        ).setLayers([
            CCLayerType.FILLED.withComponent("I"),             // 底: 铁块
            CCLayerType.EMPTY.build(),                          // 中: 空层（仅框架中无方块）
            CCLayerType.FILLED.withComponent("I")              // 顶: 铁块
        ]).setComponents(new CCBlockComponent()
            .add("I", "minecraft:iron_block")
            .build()
        ).setRecipeSize(3)                                     // 3x3 区域

    // =========================================================
    // 示例 7: 带 NBT 输出的配方
    // 自定义物品名、附魔等
    // =========================================================
    event.recipes.compactcrafting
        .miniaturization(
            Item.of('minecraft:diamond_sword', 1)
                .enchant('minecraft:sharpness', 5)
                .withName(Component.of('§c传说中的神剑§r')),
            Item.of('minecraft:netherite_ingot', 1)
        ).setLayers([
            CCLayerType.FILLED.withComponent("D"),
            CCLayerType.FILLED.withComponent("S"),
            CCLayerType.FILLED.withComponent("D")
        ]).setComponents(new CCBlockComponent()
            .add("D", "minecraft:diamond_block")
            .add("S", "minecraft:netherite_block")
            .build()
        ).setRecipeSize(1)

})

