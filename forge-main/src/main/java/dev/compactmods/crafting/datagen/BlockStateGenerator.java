package dev.compactmods.crafting.datagen;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Objects;

public class BlockStateGenerator extends BlockStateProvider {

    public BlockStateGenerator(DataGenerator gen, ExistingFileHelper files) {
        super(gen.getPackOutput(), CompactCrafting.MOD_ID, files);
    }

    @Override
    protected void registerStatesAndModels() {
        // 基础模型
        baseModel();

        // 投影仪模型
        projectorDishModel();
        projectorStaticModel();

        // 代理方块状态
        proxyBlockStates();

        // 物品模型变换
        itemTransforms();
    }

    private void baseModel() {
        BlockModelBuilder builder = models().getBuilder("block/base")
                .texture("particle", CompactCrafting.modRL("block/projector_base_bottom"));

        addProjectorBase(builder);
    }

    static void addProjectorBase(BlockModelBuilder builder) {
        builder
                .texture("base_top", CompactCrafting.modRL("block/projector_base_top"))
                .texture("base_top_cutout", CompactCrafting.modRL("block/projector_base_top_cutout"))
                .texture("base_bottom", CompactCrafting.modRL("block/projector_base_bottom"))
                .texture("base_side", CompactCrafting.modRL("block/projector_base_side"))
                .texture("pole", CompactCrafting.modRL("block/projector_pole"));

        // Base
        builder.element()
                .from(0, 0, 0)
                .to(16, 6, 16)
                .shade(true)
                .allFaces((dir, face) -> {
                    switch(dir) {
                        case NORTH:
                        case SOUTH:
                        case WEST:
                        case EAST:
                            face.texture("#base_side").uvs(0, 10, 16, 16).end();
                            break;

                        case UP:
                            face.texture("#base_top").uvs(0, 0, 16, 16).end();
                            break;

                        case DOWN:
                            face.texture("#base_bottom").uvs(0, 0, 16, 16).end();
                            break;
                    }
                })
                .end();

        builder.element()
                .from(0, 0, 0)
                .to(16, 6, 16)
                .shade(true)
                .allFaces((dir, face) -> {
                    if (Objects.requireNonNull(dir) == Direction.UP) {
                        face.texture("#base_top_cutout").uvs(0, 0, 16, 16)
                                .tintindex(1).end();
                    } else {// transparent area of texture
                        face.texture("#base_top_cutout").uvs(0, 0, 1, 1)
                                .cullface(dir.getOpposite()).end();
                    }
                })
                .end();

        builder.element()
                .from(7, 6, 7)
                .to(9, 12, 9)
                .shade(true)
                .allFaces((dir, face) -> {
                    switch(dir) {
                        case NORTH:
                        case SOUTH:
                        case WEST:
                        case EAST:
                            face.texture("#pole").uvs(0, 2, 2, 10).end();
                            break;

                        case UP:
                            face.texture("#pole").uvs(0, 0, 2, 2).end();
                            break;
                    }
                })
                .end();
    }

    private void projectorStaticModel() {
        BlockModelBuilder builder = models().getBuilder("block/field_projector_static")
                .texture("particle", modLoc("block/projector_base_bottom"));

        addProjectorBase(builder);
        addDishModel(builder);

        // 投影仪方块状态
        this.getVariantBuilder(CCBlocks.FIELD_PROJECTOR_BLOCK.get())
                .forAllStates(state -> {
                    Direction dir = state.getValue(FieldProjectorBlock.FACING);
                    boolean active = FieldProjectorBlock.isActive(state);

                    if(active) {
                        return ConfiguredModel.builder()
                                .modelFile(models().getExistingFile(modLoc("block/base")))
                                .build();
                    } else {
                        return ConfiguredModel.builder()
                                .modelFile(models().getExistingFile(modLoc("block/field_projector_static")))
                                .rotationY(((int) dir.toYRot() - 90) % 360)
                                .build();
                    }
                });
    }

    private void projectorDishModel() {
        BlockModelBuilder builder = models().getBuilder("block/field_projector_dish")
                .texture("particle", modLoc("block/projector_dish_back"));

        // Dish
        addDishModel(builder);
    }

    private void addDishModel(BlockModelBuilder builder) {
        builder
                .texture("dish_front", modLoc("block/projector_dish_front"))
                .texture("dish_front_sides", modLoc("block/projector_dish_front_sides"))
                .texture("dish_back", modLoc("block/projector_dish_back"))
                .texture("dish_connector", modLoc("block/projector_dish_connector"));

        builder.element()
                .from(4, 8, 3)
                .to(6, 16, 13)
                .allFaces((dir, face) -> {
                    switch(dir) {
                        case UP:
                            face.texture("#dish_front_sides").uvs(1, 0, 11, 1).end();
                            break;

                        case DOWN:
                            face.texture("#dish_front_sides").uvs(1, 9, 11, 10).end();
                            break;

                        case EAST:
                            face.texture("#dish_back").uvs(0, 0, 10, 8).end();
                            break;

                        case WEST:
                            face.texture("#dish_front_sides").uvs(1, 1, 11, 9).end();
                            break;

                        case NORTH:
                            face.texture("#dish_front_sides").uvs(0, 1, 1, 9).end();
                            break;

                        case SOUTH:
                            face.texture("#dish_front_sides").uvs(11, 1, 12, 9).end();
                            break;
                    }
                })
                .shade(true)
                .end();

        // Front texture
        builder.element()
                .from(4, 8, 3)
                .to(5, 16, 13)
                .allFaces((dir, face) -> {
                    if(dir == Direction.WEST) {
                        face.texture("#dish_front").uvs(0, 0, 10, 8).tintindex(0).end();
                    } else {
                        face.texture("#dish_front").uvs(0, 0, 1, 1)
                                .cullface(dir.getOpposite()).end();
                    }
                })
                .end();

        builder.element()
                .from(6, 11, 7)
                .to(7, 13, 9)
                .allFaces((dir, face) -> {
                    switch(dir) {
                        case UP:
                            face.texture("#dish_connector").uvs(1, 0, 3, 1).end();
                            break;

                        case DOWN:
                            face.texture("#dish_connector").uvs(1, 3, 3, 4).end();
                            break;

                        case EAST:
                            // back
                            face.texture("#dish_connector").uvs(1, 1, 3, 3).end();
                            break;

                        case NORTH:
                            face.texture("#dish_connector").uvs(0, 1, 1, 3).end();
                            break;

                        case SOUTH:
                            face.texture("#dish_connector").uvs(3, 1, 4, 3).end();
                            break;
                    }
                })
                .end();
    }

    private void proxyBlockStates() {
        // 匹配代理方块
        getVariantBuilder(CCBlocks.MATCH_FIELD_PROXY_BLOCK.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(models().getExistingFile(modLoc("block/base")))
                        .build());

        // 重新扫描代理方块
        getVariantBuilder(CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(models().getExistingFile(modLoc("block/base")))
                        .build());

        // 代理方块模型
        addProjectorBase(models().getBuilder("match_proxy"));
        addProjectorBase(models().getBuilder("rescan_proxy"));
    }

    private void itemTransforms() {
        // 基础物品变换
        itemModels()
                .withExistingParent("base", modLoc("block/base"))
                .transforms()
                .transform(ItemDisplayContext.GUI)
                .rotation(33.75f, 45f, 0)
                .translation(0, 1, 0)
                .scale(0.6f, 0.6f, 0.6f)
                .end();

        // 投影仪盘子物品变换
        itemModels()
                .withExistingParent("projector_dish", modLoc("block/field_projector_dish"))
                .transforms()
                .transform(ItemDisplayContext.GUI)
                .rotation(33.75f, 45f, 0)
                .translation(2, -2, 0)
                .scale(1f, 1f, 1f)
                .end();

        // 投影仪物品变换
        itemModels()
                .withExistingParent("field_projector", modLoc("block/field_projector_static"))
                .transforms()
                .transform(ItemDisplayContext.GUI)
                .rotation(33.75f, 45f, 0)
                .translation(0, 1, 0)
                .scale(0.6f, 0.6f, 0.6f)
                .end();

        // 匹配代理物品变换
        itemModels()
                .withExistingParent("match_proxy", modLoc("block/match_proxy"))
                .transforms()
                .transform(ItemDisplayContext.GUI)
                .rotation(33.75f, 45f, 0)
                .translation(0, 1, 0)
                .scale(0.6f, 0.6f, 0.6f)
                .end();

        // 重新扫描代理物品变换
        itemModels()
                .withExistingParent("rescan_proxy", modLoc("block/rescan_proxy"))
                .transforms()
                .transform(ItemDisplayContext.GUI)
                .rotation(33.75f, 45f, 0)
                .translation(0, 1, 0)
                .scale(0.6f, 0.6f, 0.6f)
                .end();
    }
}