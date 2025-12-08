ServerEvents.recipes(event => {
    event.recipes.compactcrafting
        .miniaturization(
            [Item.of('minecraft:stick',2),Item.of('minecraft:apple',64)],
            Item.of('minecraft:diamond',6),
            160,
            5
        ).setLayers([
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
            .add("WW","minecraft:white_wool")
            .add("OW","minecraft:orange_wool")
            .add("RW","minecraft:red_wool")
            .add("BW","minecraft:black_wool")
            .build()
        )
})

