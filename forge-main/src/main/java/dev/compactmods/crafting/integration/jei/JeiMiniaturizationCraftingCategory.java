package dev.compactmods.crafting.integration.jei;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.catalyst.CatalystType;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.client.ClientUtilities;
import dev.compactmods.crafting.client.fakeworld.RenderingWorld;
import dev.compactmods.crafting.client.ui.ScreenArea;
import dev.compactmods.crafting.client.render.CubeRenderHelper;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.core.CCCatalystTypes;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

public class JeiMiniaturizationCraftingCategory implements IRecipeCategory<MiniaturizationRecipe> {

    public static final ResourceLocation UID = CompactCrafting.modRL("miniaturization");
    public static final RecipeType<MiniaturizationRecipe> RECIPE_TYPE = new RecipeType<>(UID, MiniaturizationRecipe.class);

    private final IDrawable icon;
    private final BlockRenderDispatcher blocks;
    private RenderingWorld previewLevel;

    private IGuiHelper guiHelper;
    private final IDrawableStatic background;
    private final IDrawableStatic slotDrawable;
    private final IDrawableStatic arrowOutputs;

    private boolean singleLayer = false;
    private int singleLayerOffset = 0;
    private double previewRotation = 0.0d;
    private float rotationSpeed = 1.0f;
    private boolean rotatePreview = true;
    private boolean isDraggingSpeed = false;
    private boolean topView = false;
    private boolean debugMode = false;

    private ScreenArea backgroundArea = new ScreenArea(27, 0, 70, 70);
    private ScreenArea explodeToggle = new ScreenArea(30, 75, 10, 10);
    private ScreenArea layerUp = new ScreenArea(55, 75, 10, 10);
    private ScreenArea layerSwap = new ScreenArea(70, 75, 10, 10);
    private ScreenArea layerDown = new ScreenArea(85, 75, 10, 10);
    private ScreenArea speedSlider = new ScreenArea(105, 75, 35, 10);
    private ScreenArea topViewToggle = new ScreenArea(10, 75, 15, 10);
    private ScreenArea rotateControl = new ScreenArea(160, 75, 10, 10);

    /**
     * Whether the preview is exploded (expanded) or not.
     */
    private boolean exploded = false;

    /**
     * Explode multiplier; specifies how far apart blocks are rendered.
     */
    private double explodeMulti = 1.0d;
    
    /**
     * 跟踪当前鼠标悬停选中的方块位置 (用于高亮)
     */
    private BlockPos hoveredBlockPos = null;
    private int hoveredLayerY = -1;

    private final MutableComponent MATERIAL_COMPONENT = Component.translatable(CompactCrafting.MOD_ID + ".jei.miniaturization.component")
            .withStyle(ChatFormatting.GRAY)
            .withStyle(ChatFormatting.ITALIC);

    private final MutableComponent CATALYST = Component.translatable(CompactCrafting.MOD_ID + ".jei.miniaturization.catalyst")
            .withStyle(ChatFormatting.YELLOW)
            .withStyle(ChatFormatting.ITALIC);

    public JeiMiniaturizationCraftingCategory(IGuiHelper guiHelper) {
        int width = (9 * 18) + 10;
        int height = 60 + (10 + (18 * 3) + 5);

        this.guiHelper = guiHelper;
        this.background = guiHelper.createBlankDrawable(width, height);
        this.slotDrawable = guiHelper.getSlotDrawable();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(CCBlocks.FIELD_PROJECTOR_BLOCK.get()));
        this.arrowOutputs = guiHelper.createDrawable(CompactCrafting.modRL("textures/gui/jei-arrow-outputs.png"), 0, 0, 24, 19);

        this.blocks = Minecraft.getInstance().getBlockRenderer();
        this.previewLevel = null;
    }

    @Override
    public RecipeType<MiniaturizationRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    //region JEI implementation requirements
    @Override
    public Component getTitle() {
        return Component.translatable(CompactCrafting.MOD_ID + ".jei.miniaturization.title");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }
    //endregion


    @Override
    public void setRecipe(IRecipeLayoutBuilder layout, MiniaturizationRecipe recipe, IFocusGroup focuses) {
        previewLevel = new RenderingWorld(recipe);

        singleLayer = false;
        singleLayerOffset = 0;

        try {
            addMaterialSlots(recipe, layout);
            addCatalystSlots(recipe, layout);

            int fromRightEdge = this.background.getWidth() - (18 * 2) - 5;
            addOutputSlots(recipe, layout, fromRightEdge);
        } catch (Exception ex) {
            CompactCrafting.LOGGER.error(recipe.getRecipeIdentifier());
            CompactCrafting.LOGGER.error("Error displaying recipe", ex);
        }
    }

    private IRecipeSlotBuilder addCatalystSlots(MiniaturizationRecipe recipe, IRecipeLayoutBuilder layout) {
        final var catalystSlot = layout.addSlot(RecipeIngredientRole.CATALYST, 1, 1)
                .setBackground(slotDrawable, -1, -1);

        if (!recipe.getCatalyst().matches(ItemStack.EMPTY)) {
            catalystSlot.addItemStacks(new ArrayList<>(recipe.getCatalyst().getPossible()))
                    .addTooltipCallback((slots, c) -> {
                        slots.getDisplayedItemStack().ifPresent((stack) -> {
                            if (stack.getTag() != null) {
                                c.add(Component.translatable("compactcrafting.jei.catalyst.nbt").withStyle(ChatFormatting.RED));
                                if (Screen.hasShiftDown()) {
                                    c.add(NbtUtils.toPrettyComponent(stack.getTag()));
                                } else {
                                    c.add(Component.translatable("compactcrafting.jei.shift").withStyle(ChatFormatting.GOLD));
                                }
                            }
                        });
                        c.add(CATALYST);
                    });
        }
        return catalystSlot;
    }

    private void addMaterialSlots(MiniaturizationRecipe recipe, IRecipeLayoutBuilder layout) {
        AtomicInteger inputOffset = new AtomicInteger();

        final int GUTTER_X = 5;
        final int OFFSET_Y = 64;

        recipe.getComponentTotals()
                .entrySet()
                .stream()
                .filter(comp -> comp.getValue() > 0)
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach((comp) -> {
                    String component = comp.getKey();
                    int required = comp.getValue();
                    int finalInputOffset = inputOffset.get();

                    IRecipeBlockComponent bs = recipe.getComponents().getBlock(component).get();
                    if (bs instanceof BlockComponent bsc) {
                        Item bi = bsc.getBlock().asItem();

                        int slotX = GUTTER_X + (finalInputOffset % 9) * 18;
                        int slotY = (OFFSET_Y + 24) + ((finalInputOffset / 9) * 18);

                        final var slot = layout.addSlot(RecipeIngredientRole.INPUT, slotX, slotY)
                                .setBackground(slotDrawable, -1, -1);

                        if (bi != Items.AIR) {
                            slot.addItemStack(new ItemStack(bi, required));
                            slot.addTooltipCallback((slots, c) -> c.add(MATERIAL_COMPONENT));
                            inputOffset.getAndIncrement();
                        }
                    }
                });

        for (int i = inputOffset.get(); i < 18; i++) {
            int slotX = GUTTER_X + (i % 9) * 18;
            int slotY = (OFFSET_Y + 24) + ((i / 9) * 18);

            layout.addSlot(RecipeIngredientRole.INPUT, slotX, slotY).setBackground(slotDrawable, -1, -1);
        }
    }

    private void addOutputSlots(MiniaturizationRecipe recipe, IRecipeLayoutBuilder layout, int GUTTER_X) {
        final var out = recipe.getOutputs();
        for (int outputNum = 0; outputNum < 6; outputNum++) {
            int x = (18 * (outputNum % 2)) + GUTTER_X + 1;
            int y = (18 * (outputNum / 2)) + 8 + 1;

            final var slot = layout.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .setBackground(slotDrawable, -1, -1);

            if (outputNum < out.length)
                slot.addItemStack(out[outputNum]);
        }
    }
    //endregion


    @Override
    public List<Component> getTooltipStrings(MiniaturizationRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        // 重置悬停信息
        hoveredBlockPos = null;
        hoveredLayerY = -1;
        
        if (explodeToggle.contains(mouseX, mouseY)) {
            if (!exploded) return List.of(Component.translatable("compactcrafting.jei.toggle_exploded_view"));
            else return List.of(Component.translatable("compactcrafting.jei.toggle_condensed_view"));
        }

        if (layerSwap.contains(mouseX, mouseY)) {
            if (singleLayer) return List.of(Component.translatable("compactcrafting.jei.all_layers_mode"));
            else return List.of(Component.translatable("compactcrafting.jei.single_layer_mode"));
        }

        if (layerUp.contains(mouseX, mouseY) && singleLayer) {
            if (singleLayerOffset < recipe.getDimensions().getYsize() - 1)
                return List.of(Component.translatable("compactcrafting.jei.layer_up"));
        }

        if (layerDown.contains(mouseX, mouseY) && singleLayer) {
            if (singleLayerOffset > 0)
                return List.of(Component.translatable("compactcrafting.jei.layer_down"));
        }

        if (topViewToggle.contains(mouseX, mouseY)) {
            return List.of(Component.translatable("compactcrafting.jei.top_view_toggle"));
        }

        if (speedSlider.contains(mouseX, mouseY)) {
            return List.of(Component.translatable("compactcrafting.jei.rotation_speed", String.format("%.1f", rotationSpeed)));
        }

        if (rotateControl.contains(mouseX, mouseY)) {
            return List.of(Component.translatable("compactcrafting.jei.rotate_preview"));
        }

        // 检查是否鼠标在结构预览区域上有方块
        Optional<HoverInfo> hoverInfo = getHoverInfoAtMouse(recipe, mouseX, mouseY);
        if (hoverInfo.isPresent()) {
            HoverInfo info = hoverInfo.get();
            hoveredBlockPos = info.pos;
            hoveredLayerY = info.layerY;
            
            IRecipeBlockComponent component = info.component;
            if (component instanceof BlockComponent blockComp) {
                Item item = blockComp.getBlock().asItem();
                if (item != Items.AIR) {
                    ItemStack stack = new ItemStack(item);
                    List<Component> tooltip = new ArrayList<>();
                    // 添加物品名称
                    tooltip.add(stack.getHoverName());
                    // 添加额外的tooltip信息
                    tooltip.add(MATERIAL_COMPONENT);
                    return tooltip;
                }
            }
        }

        return Collections.emptyList();
    }

    @Override
    public boolean handleInput(MiniaturizationRecipe recipe, double mouseX, double mouseY, InputConstants.Key input) {
        if (input.getType() == InputConstants.Type.MOUSE && input.getValue() == 0) {
            SoundManager handler = Minecraft.getInstance().getSoundManager();

            if (explodeToggle.contains(mouseX, mouseY)) {
                explodeMulti = exploded ? 1.0d : 1.6d;
                exploded = !exploded;
                handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            if (layerSwap.contains(mouseX, mouseY)) {
                singleLayer = !singleLayer;
                handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            if (layerUp.contains(mouseX, mouseY) && singleLayer) {
                if (singleLayerOffset < recipe.getDimensions().getYsize() - 1) {
                    handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    singleLayerOffset++;
                }
                return true;
            }

            if (layerDown.contains(mouseX, mouseY) && singleLayer) {
                if (singleLayerOffset > 0) {
                    handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    singleLayerOffset--;
                }

                return true;
            }

            if (speedSlider.contains(mouseX, mouseY)) {
                isDraggingSpeed = true;
                rotationSpeed = calcSpeedFromMouse(mouseX);
                handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            if (topViewToggle.contains(mouseX, mouseY)) {
                topView = !topView;
                handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            if (rotateControl.contains(mouseX, mouseY)) {
                rotatePreview = !rotatePreview;
                handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }


        }
        // Todo: 添加更多预览区域控制
//        if (backgroundArea.contains(mouseX, mouseY) && ClientUtilities.isDebugScreenOpen()) {
//            CompactCrafting.ClientPlayerTell(input.getType().name()+input.getValue());
//            return true;
//        }

        return false;
    }

    //region Rendering help
    private void drawScaledTexture(
            GuiGraphics guiGraphics,
            ResourceLocation texture,
            ScreenArea area,
            float u, float v,
            int uWidth, int vHeight,
            int textureWidth, int textureHeight) {

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.enableDepthTest();
        guiGraphics.blit(texture, area.x, area.y, area.width, area.height, u, v, uWidth, vHeight, textureWidth, textureHeight);
    }

    //endregion

    @Override
    public void draw(MiniaturizationRecipe recipe, IRecipeSlotsView slots, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        AABB dims = recipe.getDimensions();

        Window mainWindow = Minecraft.getInstance().getWindow();

        drawScaledTexture(guiGraphics,
                CompactCrafting.modRL("textures/gui/jei-arrow-field.png"),
                new ScreenArea(7, 20, 17, 22),
                0, 0, 17, 22, 17, 22);

        drawScaledTexture(guiGraphics,
                CompactCrafting.modRL("textures/gui/jei-arrow-outputs.png"),
                new ScreenArea(100, 25, 24, 19),
                0, 0, 24, 19, 24, 19);

        int scissorX = 27;
        int scissorY = 0;

        double guiScaleFactor = mainWindow.getGuiScale();
        ScreenArea scissorBounds = new ScreenArea(
                scissorX, scissorY,
                70,
                70
        );

        renderPreviewControls(guiGraphics, dims);

        // 处理滑块拖拽
        if (isDraggingSpeed) {
            long window = Minecraft.getInstance().getWindow().getWindow();
            if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
                rotationSpeed = calcSpeedFromMouse(mouseX);
            } else {
                isDraggingSpeed = false;
            }
        }

        // 更新悬停信息（确保高亮能正确显示
        updateHoveredInfo(recipe, mouseX, mouseY);

        if (previewLevel != null) renderRecipe(recipe, guiGraphics, dims, guiScaleFactor, scissorBounds);
    }
    
    /**
     * 更新悬停信息
     */
    private void updateHoveredInfo(MiniaturizationRecipe recipe, double mouseX, double mouseY) {
        Optional<HoverInfo> hoverInfo = getHoverInfoAtMouse(recipe, mouseX, mouseY);
        if (hoverInfo.isPresent()) {
            HoverInfo info = hoverInfo.get();
            hoveredBlockPos = info.pos;
            hoveredLayerY = info.layerY;
        } else {
            hoveredBlockPos = null;
            hoveredLayerY = -1;
        }
    }

    private void renderRecipe(MiniaturizationRecipe recipe, GuiGraphics guiGraphics, AABB dims, double guiScaleFactor, ScreenArea scissorBounds) {
        try {
            guiGraphics.fill(
                    scissorBounds.x, scissorBounds.y,
                    scissorBounds.x + scissorBounds.width,
                    scissorBounds.height,
                    0xFF404040
            );

            PoseStack mx = guiGraphics.pose();

            MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();

            final double scale = Minecraft.getInstance().getWindow().getGuiScale();
            final Matrix4f matrix = mx.last().pose();
            final FloatBuffer buf = BufferUtils.createFloatBuffer(16);
            matrix.get(buf);

            // { x, y, z }
            Vec3 translation = new Vec3(
                    buf.get(12) * scale,
                    buf.get(13) * scale,
                    buf.get(14) * scale);

            scissorBounds.x *= scale;
            scissorBounds.y *= scale;
            scissorBounds.width *= scale;
            scissorBounds.height *= scale;
            final int scissorX = Math.round(Math.round(translation.x + scissorBounds.x));
            final int scissorY = Math.round(Math.round(Minecraft.getInstance().getWindow().getHeight() - scissorBounds.y - scissorBounds.height - translation.y));
            final int scissorW = scissorBounds.width;
            final int scissorH = scissorBounds.height;
            RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);

            mx.pushPose();

            mx.translate(
                    27 + (35),
                    scissorBounds.y + (35),
                    100);

            Vec3 dimsVec = new Vec3(dims.getXsize(), dims.getYsize(), dims.getZsize());
            float recipeAvgDim = (float) dimsVec.length();
            float previewScale = (float) ((3 + Math.exp(3 - (recipeAvgDim / 5))) / explodeMulti);
            mx.scale(previewScale, -previewScale, previewScale);

            drawActualRecipe(recipe, guiGraphics, dims, buffers);

            mx.popPose();

            buffers.endBatch();

            RenderSystem.disableScissor();
        } catch (Exception ex) {
            CompactCrafting.LOGGER.warn(ex);
        }
    }

    private void drawActualRecipe(MiniaturizationRecipe recipe, GuiGraphics guiGraphics, AABB dims, MultiBufferSource.BufferSource buffers) {
        PoseStack mx = guiGraphics.pose();
        previewRotation += (rotatePreview ? rotationSpeed : 0);
        if (topView) {
            mx.mulPose(new Quaternionf().rotationXYZ(
                    (float) Math.toRadians(90f), 0, 0));
        } else {
            mx.mulPose(new Quaternionf().rotationXYZ(
                    (float) Math.toRadians(35f),
                    (float) Math.toRadians(-previewRotation),
                    0));
        }

        double ySize = recipe.getDimensions().getYsize();

        // Variable explode based on mouse position (clamped)
        // double explodeMulti = MathHelper.clamp(mouseX, 0, this.background.getWidth())/this.background.getWidth()*2+1;

        int[] renderLayers;
        if (!singleLayer) {
            renderLayers = IntStream.range(0, (int) ySize).toArray();
        } else {
            renderLayers = new int[]{singleLayerOffset};
        }

        mx.translate(
                -(dims.getXsize() / 2) * explodeMulti - 0.5,
                -(dims.getYsize() / 2) * explodeMulti - 0.5,
                -(dims.getZsize() / 2) * explodeMulti - 0.5
        );

        for (int y : renderLayers) {
            recipe.getLayer(y).ifPresent(l -> renderRecipeLayer(recipe, guiGraphics, buffers, l, y));
        }
        
        // 渲染高亮（如果有的话）
        if (hoveredBlockPos != null && hoveredLayerY != -1) {
            renderHighlightAtPosition(recipe, guiGraphics, buffers, hoveredBlockPos, hoveredLayerY, dims);
        }
    }
    
    /**
     * 在指定位置渲染高亮
     */
    private void renderHighlightAtPosition(MiniaturizationRecipe recipe, GuiGraphics guiGraphics, 
                                          MultiBufferSource.BufferSource buffers, BlockPos pos, int layerY, AABB dims) {
        PoseStack mx = guiGraphics.pose();
        mx.pushPose();
        
        // 移到目标方块位置（和 renderRecipeLayer 中的逻辑一致）
        mx.translate(
                ((pos.getX() + 0.5) * explodeMulti),
                ((layerY + 0.5) * explodeMulti),
                ((pos.getZ() + 0.5) * explodeMulti)
        );
        
        // 渲染高亮框
        renderHighlightBox(guiGraphics, buffers, mx);
        
        mx.popPose();
    }

    private void renderPreviewControls(GuiGraphics guiGraphics, AABB dims) {
        PoseStack mx = guiGraphics.pose();
        mx.pushPose();
        mx.translate(0, 0, 10);

        ResourceLocation sprites = CompactCrafting.modRL("textures/gui/jei-sprites.png");

        if (exploded) {
            drawScaledTexture(guiGraphics, sprites, explodeToggle, 20, 0, 20, 20, 160, 20);
        } else {
            drawScaledTexture(guiGraphics, sprites, explodeToggle, 0, 0, 20, 20, 160, 20);
        }

        // Layer change buttons
        if (singleLayer) {
            drawScaledTexture(guiGraphics, sprites, layerSwap, 60, 0, 20, 20, 160, 20);
        } else {
            drawScaledTexture(guiGraphics, sprites, layerSwap, 40, 0, 20, 20, 160, 20);
        }

        if (singleLayer) {
            if (singleLayerOffset < dims.getYsize() - 1)
                drawScaledTexture(guiGraphics, sprites, layerUp, 80, 0, 20, 20, 160, 20);

            if (singleLayerOffset > 0) {
                drawScaledTexture(guiGraphics, sprites, layerDown, 100, 0, 20, 20, 160, 20);
            }
        }

        // 绘制速度滑块
        Font sliderFont = Minecraft.getInstance().font;
        String speedLabel = String.format("%.1f", rotationSpeed);

        // 滑块轨道（2px高的灰色线条）
        int trackX = speedSlider.x;
        int trackY = speedSlider.y + speedSlider.height / 2 - 1;
        int trackWidth = speedSlider.width;
        guiGraphics.fill(trackX, trackY, trackX + trackWidth, trackY + 2, 0xFF888888);

        // 滑块手柄（白色矩形）
        float speedRatio = Math.min(1.0f, rotationSpeed / 5.0f);
        int handleX = trackX + (int) (trackWidth * speedRatio);
        int handleSize = 6;
        guiGraphics.fill(handleX - handleSize / 2, trackY - 2, handleX + handleSize / 2, trackY + 4, 0xFFFFFFFF);

        // 速度标签（滑块上方）
        guiGraphics.drawString(sliderFont, speedLabel, speedSlider.x, speedSlider.y - 9, 0xFFFFFFFF);

        // 俯视图按钮
        int tvColor = topView ? 0xFFFFFF88 : 0x88FFFFFF;
        guiGraphics.fill(topViewToggle.x, topViewToggle.y,
                topViewToggle.x + topViewToggle.width, topViewToggle.y + topViewToggle.height,
                tvColor);
        guiGraphics.drawString(sliderFont, "TV",
                topViewToggle.x + 1, topViewToggle.y + 1,
                topView ? 0xFF000000 : 0xFFFFFFFF);

        // 播放/暂停按钮
        if (rotatePreview) {
            drawScaledTexture(guiGraphics, sprites, rotateControl, 140, 0, 20, 20, 160, 20);
        } else {
            drawScaledTexture(guiGraphics, sprites, rotateControl, 120, 0, 20, 20, 160, 20);
        }

        mx.popPose();
    }

    private void renderRecipeLayer(MiniaturizationRecipe recipe, GuiGraphics guiGraphics, MultiBufferSource.BufferSource buffers, IRecipeLayer l, int layerY) {
        PoseStack mx = guiGraphics.pose();
        // Begin layer
        mx.pushPose();

        AABB layerBounds = BlockSpaceUtil.getLayerBounds(recipe.getDimensions(), layerY);
        BlockPos.betweenClosedStream(layerBounds).forEach(filledPos -> {
            mx.pushPose();

            mx.translate(
                    ((filledPos.getX() + 0.5) * explodeMulti),
                    ((layerY + 0.5) * explodeMulti),
                    ((filledPos.getZ() + 0.5) * explodeMulti)
            );

            BlockPos zeroedPos = filledPos.below(layerY);
            Optional<String> componentForPosition = l.getComponentForPosition(zeroedPos);
            componentForPosition
                    .flatMap(recipe.getComponents()::getBlock)
                    .ifPresent(comp -> renderComponent(guiGraphics, buffers, comp, filledPos));

            mx.popPose();
        });

        // Done with layer
        mx.popPose();
    }

    private void renderComponent(GuiGraphics guiGraphics, MultiBufferSource.BufferSource buffers, IRecipeBlockComponent state, BlockPos filledPos) {
        // TODO - Render switching at fixed interval
        if (state.didErrorRendering())
            return;

        BlockState state1 = state.getRenderState();

        ModelData data = ModelData.EMPTY;
        if (previewLevel != null && state1.hasBlockEntity()) {
            // create fake world instance
            // get tile entity - extend EmptyBlockReader with impl
            BlockEntity be = previewLevel.getBlockEntity(filledPos);
            if (be != null)
                data = be.getModelData();
        }
        PoseStack mx = guiGraphics.pose();
        try {
            // TODO: Revisit render types
            blocks.renderSingleBlock(state1,
                    mx,
                    buffers,
                    LightTexture.FULL_SKY,
                    OverlayTexture.NO_OVERLAY,
                    data, RenderType.cutout());
        } catch (Exception e) {
            state.markRenderingErrored();

            CompactCrafting.LOGGER.warn("Error rendering block in preview: {}", state1);
            CompactCrafting.LOGGER.error("Stack Trace", e);
        }
    }
    
    /**
     * 渲染高亮框
     */
    private void renderHighlightBox(GuiGraphics guiGraphics, MultiBufferSource.BufferSource buffers, PoseStack poseStack) {
        // 使用明亮的白色高亮（ARGB格式）- 更容易看到
        int color = FastColor.ARGB32.color(255, 255, 255, 255);
        
        // 稍微放大一点，让高亮框包围在方块外面
        float expand = 0.05f;
        AABB box = new AABB(
                -0.5f - expand, -0.5f - expand, -0.5f - expand,
                0.5f + expand, 0.5f + expand, 0.5f + expand);
        
        // 渲染高亮框的线框
        VertexConsumer vertexConsumer = buffers.getBuffer(RenderType.lines());
        
        // 前面四条边
        drawLine(vertexConsumer, poseStack, color, 
                new Vec3(box.minX, box.minY, box.maxZ), 
                new Vec3(box.maxX, box.minY, box.maxZ));
        drawLine(vertexConsumer, poseStack, color, 
                new Vec3(box.maxX, box.minY, box.maxZ), 
                new Vec3(box.maxX, box.maxY, box.maxZ));
        drawLine(vertexConsumer, poseStack, color, 
                new Vec3(box.maxX, box.maxY, box.maxZ), 
                new Vec3(box.minX, box.maxY, box.maxZ));
        drawLine(vertexConsumer, poseStack, color, 
                new Vec3(box.minX, box.maxY, box.maxZ), 
                new Vec3(box.minX, box.minY, box.maxZ));
        
        // 后面四条边
        drawLine(vertexConsumer, poseStack, color, 
                new Vec3(box.minX, box.minY, box.minZ), 
                new Vec3(box.maxX, box.minY, box.minZ));
        drawLine(vertexConsumer, poseStack, color, 
                new Vec3(box.maxX, box.minY, box.minZ), 
                new Vec3(box.maxX, box.maxY, box.minZ));
        drawLine(vertexConsumer, poseStack, color, 
                new Vec3(box.maxX, box.maxY, box.minZ), 
                new Vec3(box.minX, box.maxY, box.minZ));
        drawLine(vertexConsumer, poseStack, color, 
                new Vec3(box.minX, box.maxY, box.minZ), 
                new Vec3(box.minX, box.minY, box.minZ));
        
        // 连接前后的边
        drawLine(vertexConsumer, poseStack, color, 
                new Vec3(box.minX, box.minY, box.minZ), 
                new Vec3(box.minX, box.minY, box.maxZ));
        drawLine(vertexConsumer, poseStack, color, 
                new Vec3(box.maxX, box.minY, box.minZ), 
                new Vec3(box.maxX, box.minY, box.maxZ));
        drawLine(vertexConsumer, poseStack, color, 
                new Vec3(box.maxX, box.maxY, box.minZ), 
                new Vec3(box.maxX, box.maxY, box.maxZ));
        drawLine(vertexConsumer, poseStack, color, 
                new Vec3(box.minX, box.maxY, box.minZ), 
                new Vec3(box.minX, box.maxY, box.maxZ));
    }
    
    /**
     * 绘制一条线
     */
    private void drawLine(VertexConsumer consumer, PoseStack poseStack, int color, Vec3 start, Vec3 end) {
        CubeRenderHelper.drawLine(consumer, poseStack, color, start, end);
    }

    /**
     * 根据鼠标X位置计算旋转速度 (0.0 ~ 5.0)
     */
    private float calcSpeedFromMouse(double mouseX) {
        double relX = mouseX - speedSlider.x;
        relX = Math.max(0, Math.min(relX, speedSlider.width));
        float ratio = (float) (relX / speedSlider.width);
        return Math.round(ratio * 50.0f) / 10.0f;
    }

    // 辅助记录类来保存选中方块的信息
    private static class HoverInfo {
        IRecipeBlockComponent component;
        BlockPos pos;
        int layerY;
        
        HoverInfo(IRecipeBlockComponent component, BlockPos pos, int layerY) {
            this.component = component;
            this.pos = pos;
            this.layerY = layerY;
        }
    }

    private Optional<HoverInfo> getHoverInfoAtMouse(MiniaturizationRecipe recipe, double mouseX, double mouseY) {
        // 仅俯视图模式下启用悬停检测
        if (!topView) return Optional.empty();
        if (previewLevel == null) return Optional.empty();
        if (!backgroundArea.contains(mouseX, mouseY)) return Optional.empty();

        AABB dims = recipe.getDimensions();

        // 计算预览缩放
        Vec3 dimsVec = new Vec3(dims.getXsize(), dims.getYsize(), dims.getZsize());
        float recipeAvgDim = (float) dimsVec.length();
        float previewScale = (float) ((3 + Math.exp(3 - (recipeAvgDim / 5))) / explodeMulti);

        double previewCenterX = backgroundArea.x + 35;
        double previewCenterY = 35;

        // 中心偏移量
        double cx = -(dims.getXsize() / 2 * explodeMulti + 0.5);
        double cz = -(dims.getZsize() / 2 * explodeMulti + 0.5);

        double e = explodeMulti;
        // 俯视图下：Rx(90°) 使配方平面X→屏幕X，Z→屏幕Y
        // 方块在屏幕上占 previewScale 像素（方块始终1x1，不随explode缩放）
        // 检测半径 = previewScale * 0.5（半宽）放宽到 95% 避免边缘漏检
        double detectionRadius = previewScale * 0.95;

        // 从上层到下层遍历（上层方块优先）
        int[] renderLayers;
        if (!singleLayer) {
            renderLayers = IntStream.range(0, (int) dims.getYsize()).toArray();
        } else {
            renderLayers = new int[]{singleLayerOffset};
        }

        record Candidate(HoverInfo info, double dist) {}
        List<Candidate> candidates = new ArrayList<>();

        for (int layerIdx = renderLayers.length - 1; layerIdx >= 0; layerIdx--) {
            int layerY = renderLayers[layerIdx];
            final int finalLayerY = layerY;
            Optional<IRecipeLayer> layerOpt = recipe.getLayer(layerY);
            if (layerOpt.isEmpty()) continue;

            IRecipeLayer layer = layerOpt.get();
            AABB layerBounds = BlockSpaceUtil.getLayerBounds(dims, layerY);

            // 正向投影每个方块到屏幕，找离鼠标最近的
            BlockPos.betweenClosedStream(layerBounds).forEach(filledPos -> {
                BlockPos zeroedPos = filledPos.below(finalLayerY);
                Optional<String> componentForPosition = layer.getComponentForPosition(zeroedPos);
                componentForPosition.flatMap(recipe.getComponents()::getBlock).ifPresent(comp -> {
                    double sx = ((filledPos.getX() + 0.5) * e + cx) * previewScale + previewCenterX;
                    double sz = ((filledPos.getZ() + 0.5) * e + cz) * previewScale + previewCenterY;
                    double dx = sx - mouseX;
                    double dz = sz - mouseY;
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist < detectionRadius) {
                        candidates.add(new Candidate(
                            new HoverInfo(comp, filledPos, finalLayerY), dist));
                    }
                });
            });
        }

        // 选择最近的
        if (!candidates.isEmpty()) {
            candidates.sort(Comparator.comparingDouble(Candidate::dist));
            return Optional.of(candidates.get(0).info);
        }

        return Optional.empty();
    }
}