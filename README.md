# Compact Crafting

<p style="text-align: center;">
  <a href="https://discord.gg/Y5QhUWxQdq">
    <img src="https://img.shields.io/discord/765363477186740234?label=Discord&amp;logo=discord&amp;logoColor=white&amp;style=for-the-badge" alt="Discord" />
  </a>
</p>

This mod is based off of the Miniaturization Crafting mechanic from Compact Machines pre-1.13. It's still
a work in progress, but most of the major features are in place and operational. Here is a summary:

## Feature: Data-driven Recipes
All the recipes are loaded from the `data/<container>/recipes` folder, the same as any other datapack-driven recipe. [See the wiki][RecipeSpec] for some examples, or click [here](recipes/diamond_block.json) for a full example file.

## Feature: Recipe layers
Previously, the crafting mechanic forced a designer to be incredibly explicit about how recipe layers were defined. Given
that this means a lot more work on a designer rather than letting the mod itself "figure it out," it meant that custom recipes
felt like a second-class citizen to the mod. In Compact Crafting, this system was overhauled to make layer definitions 
more streamlined.

Take a look at the wiki's [Recipe Layer Specification][RecipeLayerSpec] to see more information.

## Feature: Proxy Blocks
To be added in a future release. Allows for better automation of the miniaturization field.

---

# KubeJS Integration

Compact Crafting provides a KubeJS plugin that allows you to create miniaturization recipes via KubeJS server scripts.

## Layer Types

| Type | Description | Requires `recipeSize` |
|---|---|---|
| `MIXED` | Define each cell with a 2D pattern grid. `"-"` means air. | No |
| `FILLED` | Fill the entire layer with a single component. | Yes |
| `HOLLOW` | Walls only (perimeter filled, interior empty). | Yes |
| `EMPTY` | Entire layer is empty (air only). | Yes |

> **Note:** `recipeSize` is required when using `FILLED`, `HOLLOW` or `EMPTY` layers, as these layers dynamically determine their footprint from the field size parameter. When used alongside `MIXED` layers (which have a baked-in footprint), the `recipeSize` can be omitted — the largest fixed layer's footprint is used instead.

## Building Blocks

### `CCBlockComponent`
Define the block components used in your recipe:

```javascript
new CCBlockComponent()
    .add("WW", "minecraft:white_wool")
    .add("OW", "minecraft:orange_wool")
    .add("RW", "minecraft:red_wool")
    .add("BW", "minecraft:black_wool")
    .build()
```

### `CCLayerType`
Create layer definitions for each layer type:

```javascript
// MIXED layer — define a 2D pattern grid
CCLayerType.MIXED.withPattern([
    ["A", "A", "A"],
    ["A", "-", "A"],
    ["A", "A", "A"]
])

// FILLED layer — fill the whole layer with one component
CCLayerType.FILLED.withComponent("G")

// HOLLOW layer — walls only
CCLayerType.HOLLOW.withWall("G")

// EMPTY layer — nothing
CCLayerType.EMPTY.build()
```

### `setRecipeSize(n)`
Set the recipe footprint size (required for `FILLED`/`HOLLOW`/`EMPTY` layers).

### `setCraftingTime(n)`
Set the crafting time in ticks (default: 200 = 10 seconds).

---

## Examples

### 1. All MIXED Layers — Chicken Spawn Egg

```javascript
ServerEvents.recipes(event => {
    event.recipes.compactcrafting
        .miniaturization(
            Item.of('minecraft:chicken_spawn_egg', 1),
            Item.of('minecraft:egg', 1)
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
})
```

### 2. All FILLED Layers — Compact Walls

```javascript
ServerEvents.recipes(event => {
    event.recipes.compactcrafting
        .miniaturization(
            Item.of('minecraft:diamond', 1),
            Item.of('minecraft:ender_pearl', 5)
        ).setLayers([
            CCLayerType.FILLED.withComponent("R"),
            CCLayerType.FILLED.withComponent("I")
        ]).setComponents(new CCBlockComponent()
            .add("R", "minecraft:redstone_wire")
            .add("I", "minecraft:iron_block")
            .build()
        ).setRecipeSize(1)
})
```

### 3. Mixed Types — End Crystal (FILLED + HOLLOW + MIXED)

```javascript
ServerEvents.recipes(event => {
    event.recipes.compactcrafting
        .miniaturization(
            [Item.of('minecraft:end_crystal', 4)],
            Item.of('minecraft:ender_pearl', 1)
        ).setLayers([
            CCLayerType.FILLED.withComponent("G"),
            CCLayerType.HOLLOW.withWall("G"),
            CCLayerType.MIXED.withPattern([
                ["G", "G", "G", "G", "G"],
                ["G", "-", "-", "-", "G"],
                ["G", "-", "O", "-", "G"],
                ["G", "-", "-", "-", "G"],
                ["G", "G", "G", "G", "G"]
            ]),
            CCLayerType.HOLLOW.withWall("G"),
            CCLayerType.FILLED.withComponent("G")
        ]).setComponents(new CCBlockComponent()
            .add("G", "minecraft:glass")
            .add("O", "minecraft:obsidian")
            .build()
        ).setRecipeSize(5)
})
```

### 4. Multiple Outputs + NBT Catalyst

```javascript
ServerEvents.recipes(event => {
    event.recipes.compactcrafting
        .miniaturization(
            [
                Item.of('minecraft:diamond', 1),
                Item.of('minecraft:iron_ingot', 4)
            ],
            Item.of('minecraft:enchanted_book').enchant('minecraft:protection', 4),
            160,
            3
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
})
```

### 5. HOLLOW Only — Glass Box (3×3×3)

```javascript
ServerEvents.recipes(event => {
    event.recipes.compactcrafting
        .miniaturization(
            Item.of('minecraft:glass', 16),
            Item.of('minecraft:glass_pane', 1)
        ).setLayers([
            CCLayerType.FILLED.withComponent("G"),
            CCLayerType.HOLLOW.withWall("G"),
            CCLayerType.FILLED.withComponent("G")
        ]).setComponents(new CCBlockComponent()
            .add("G", "minecraft:glass")
            .build()
        ).setRecipeSize(3)
})
```

### 6. With EMPTY Layer

```javascript
ServerEvents.recipes(event => {
    event.recipes.compactcrafting
        .miniaturization(
            Item.of('minecraft:iron_block', 2),
            Item.of('minecraft:iron_ingot', 1)
        ).setLayers([
            CCLayerType.FILLED.withComponent("I"),
            CCLayerType.EMPTY.build(),
            CCLayerType.FILLED.withComponent("I")
        ]).setComponents(new CCBlockComponent()
            .add("I", "minecraft:iron_block")
            .build()
        ).setRecipeSize(3)
})
```

### 7. NBT Output — Custom Enchanted Sword

```javascript
ServerEvents.recipes(event => {
    event.recipes.compactcrafting
        .miniaturization(
            Item.of('minecraft:diamond_sword', 1)
                .enchant('minecraft:sharpness', 5)
                .withName(Component.of('§cLegendary Sword§r')),
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
```

---

# KubeJS 集成

Compact Crafting 提供了 KubeJS 插件，让你可以通过 KubeJS 服务端脚本创建微缩合成配方。

## 层类型

| 类型 | 描述 | 需要 `recipeSize` |
|---|---|---|
| `MIXED` | 通过二维网格精确定义每个位置的方块。`"-"` 代表空。 | 否 |
| `FILLED` | 整个层填满同一种方块。 | 是 |
| `HOLLOW` | 仅外层墙壁（外圈填充，内部空心）。 | 是 |
| `EMPTY` | 整个层均为空。 | 是 |

> **注意：** 使用 `FILLED`、`HOLLOW` 或 `EMPTY` 层时必须指定 `recipeSize`，因为这些层需要场尺寸参数来确定其占地区域。如果配方中也包含 `MIXED` 层（其尺寸由网格确定），则 `recipeSize` 可以省略，系统会自动采用最大的固定层尺寸。

## 构建工具

### `CCBlockComponent`
定义配方中用到的方块组件：

```javascript
new CCBlockComponent()
    .add("WW", "minecraft:white_wool")
    .add("OW", "minecraft:orange_wool")
    .add("RW", "minecraft:red_wool")
    .add("BW", "minecraft:black_wool")
    .build()
```

### `CCLayerType`
创建各类型的层定义：

```javascript
// MIXED 层 — 定义二维网格
CCLayerType.MIXED.withPattern([
    ["A", "A", "A"],
    ["A", "-", "A"],
    ["A", "A", "A"]
])

// FILLED 层 — 整层填满同种方块
CCLayerType.FILLED.withComponent("G")

// HOLLOW 层 — 仅外圈墙壁
CCLayerType.HOLLOW.withWall("G")

// EMPTY 层 — 全空
CCLayerType.EMPTY.build()
```

### `setRecipeSize(n)`
设置配方占地区域大小（`FILLED`/`HOLLOW`/`EMPTY` 层需要）。

### `setCraftingTime(n)`
设置合成时间（tick，默认 200 = 10 秒）。

---

## 示例

### 1. 全 MIXED 层 — 鸡刷怪蛋

```javascript
ServerEvents.recipes(event => {
    event.recipes.compactcrafting
        .miniaturization(
            Item.of('minecraft:chicken_spawn_egg', 1),
            Item.of('minecraft:egg', 1)
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
})
```

### 2. 全 FILLED 层 — 压缩墙壁

```javascript
ServerEvents.recipes(event => {
    event.recipes.compactcrafting
        .miniaturization(
            Item.of('minecraft:diamond', 1),
            Item.of('minecraft:ender_pearl', 5)
        ).setLayers([
            CCLayerType.FILLED.withComponent("R"),
            CCLayerType.FILLED.withComponent("I")
        ]).setComponents(new CCBlockComponent()
            .add("R", "minecraft:redstone_wire")
            .add("I", "minecraft:iron_block")
            .build()
        ).setRecipeSize(1)
})
```

### 3. 混合层 — 末影水晶 (FILLED + HOLLOW + MIXED)

```javascript
ServerEvents.recipes(event => {
    event.recipes.compactcrafting
        .miniaturization(
            [Item.of('minecraft:end_crystal', 4)],
            Item.of('minecraft:ender_pearl', 1)
        ).setLayers([
            CCLayerType.FILLED.withComponent("G"),
            CCLayerType.HOLLOW.withWall("G"),
            CCLayerType.MIXED.withPattern([
                ["G", "G", "G", "G", "G"],
                ["G", "-", "-", "-", "G"],
                ["G", "-", "O", "-", "G"],
                ["G", "-", "-", "-", "G"],
                ["G", "G", "G", "G", "G"]
            ]),
            CCLayerType.HOLLOW.withWall("G"),
            CCLayerType.FILLED.withComponent("G")
        ]).setComponents(new CCBlockComponent()
            .add("G", "minecraft:glass")
            .add("O", "minecraft:obsidian")
            .build()
        ).setRecipeSize(5)
})
```

### 4. 多输出 + NBT 催化剂

```javascript
ServerEvents.recipes(event => {
    event.recipes.compactcrafting
        .miniaturization(
            [
                Item.of('minecraft:diamond', 1),
                Item.of('minecraft:iron_ingot', 4)
            ],
            Item.of('minecraft:enchanted_book').enchant('minecraft:protection', 4),
            160,
            3
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
})
```

### 5. 纯 HOLLOW 层 — 玻璃箱 (3×3×3)

```javascript
ServerEvents.recipes(event => {
    event.recipes.compactcrafting
        .miniaturization(
            Item.of('minecraft:glass', 16),
            Item.of('minecraft:glass_pane', 1)
        ).setLayers([
            CCLayerType.FILLED.withComponent("G"),
            CCLayerType.HOLLOW.withWall("G"),
            CCLayerType.FILLED.withComponent("G")
        ]).setComponents(new CCBlockComponent()
            .add("G", "minecraft:glass")
            .build()
        ).setRecipeSize(3)
})
```

### 6. 含空层配方

```javascript
ServerEvents.recipes(event => {
    event.recipes.compactcrafting
        .miniaturization(
            Item.of('minecraft:iron_block', 2),
            Item.of('minecraft:iron_ingot', 1)
        ).setLayers([
            CCLayerType.FILLED.withComponent("I"),
            CCLayerType.EMPTY.build(),
            CCLayerType.FILLED.withComponent("I")
        ]).setComponents(new CCBlockComponent()
            .add("I", "minecraft:iron_block")
            .build()
        ).setRecipeSize(3)
})
```

### 7. NBT 输出 — 自定义附魔剑

```javascript
ServerEvents.recipes(event => {
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
```

[RecipeSpec]: https://github.com/CompactMods/CompactCrafting/wiki/Recipe-Specification
[RecipeLayerSpec]: https://github.com/CompactMods/CompactCrafting/wiki/Recipe-Layer-Specification
