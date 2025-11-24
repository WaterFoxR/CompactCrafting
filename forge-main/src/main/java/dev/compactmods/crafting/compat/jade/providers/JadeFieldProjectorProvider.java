package dev.compactmods.crafting.compat.jade.providers;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.core.CCCapabilities;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.projector.FieldProjectorEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.ui.ProgressArrowElement;

import java.util.Set;

public enum JadeFieldProjectorProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    
    INSTANCE;

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(CompactCrafting.MOD_ID, "field_projector");
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockState state = accessor.getBlockState();
        if (!(state.getBlock() instanceof FieldProjectorBlock) || !FieldProjectorBlock.isActive(state)) {
            return;
        }

        // 从服务器数据获取信息
        CompoundTag serverData = accessor.getServerData();
        if (serverData.contains("hasRecipe")) {
            boolean hasRecipe = serverData.getBoolean("hasRecipe");
            if (!hasRecipe) {
                return;
            }

            IElementHelper helper = IElementHelper.get();
            
            // 添加配方标题(显示“当前配方”4个字)
            tooltip.add(Component.translatable(CompactCrafting.MOD_ID + ".jade.current_recipe")
                    .withStyle(ChatFormatting.GRAY));

            Vec2 itemSize = new Vec2(20,20);

            if(serverData.contains("catalystItem")&&serverData.contains("outputItems")){
                ItemStack catalystItem = ItemStack.of(serverData.getCompound("catalystItem"));
                CompoundTag outputItems = serverData.getCompound("outputItems");

                //添加催化剂物品
                tooltip.add(helper.item(catalystItem).size(itemSize));

                //添加进度条
                boolean b = false;
                int progress = 0;
                int totalTime;
                if (serverData.contains("progress") && serverData.contains("totalTime")){
                    b=true;
                    progress = serverData.getInt("progress");
                    totalTime = serverData.getInt("totalTime");
                    tooltip.append(new ProgressArrowElement((float)progress / (float)totalTime));
                }

                //添加输出物品
                for (String key : outputItems.getAllKeys()) {
                    ItemStack output = ItemStack.of(outputItems.getCompound(key));
                    tooltip.append(helper.item(output).size(itemSize)
                            .message(String.valueOf(Component.translatable(CompactCrafting.MOD_ID + ".jade.output")
                                    .withStyle(ChatFormatting.GRAY))));
                }

                //添加状态信息
                if(b){

                    EnumCraftingState stateEnum = EnumCraftingState.values()[serverData.getInt("craftingState")];
                    if (stateEnum == EnumCraftingState.MATCHED && progress == 0) {
                        tooltip.add(Component.translatable(CompactCrafting.MOD_ID + ".jade.awaiting_catalyst")
                                .withStyle(ChatFormatting.YELLOW));
                    }
                }
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockState state = accessor.getBlockState();
        if (!(state.getBlock() instanceof FieldProjectorBlock)) {
            return;
        }

        // 添加服务器端信息
        if (FieldProjectorBlock.isActive(state) && accessor.getBlockEntity() instanceof FieldProjectorEntity mFieldEntity) {
            mFieldEntity.getCapability(CCCapabilities.MINIATURIZATION_FIELD)
                    .ifPresent(field -> {
                        IMiniaturizationRecipe recipe = field.getCurrentRecipe().orElse(null);
                        
                        if (recipe != null) {
                            data.putBoolean("hasRecipe", true);
                            
                            int progress = field.getProgress();
                            EnumCraftingState craftingState = field.getCraftingState();
                            
                            data.putInt("progress", progress);
                            data.putInt("totalTime", recipe.getCraftingTime());
                            data.putInt("craftingState", craftingState.ordinal());
                            
                            // 保存催化剂信息
                            Set<ItemStack> possibleCatalysts = recipe.getCatalyst().getPossible();
                            if (!possibleCatalysts.isEmpty()) {
                                ItemStack catalyst = possibleCatalysts.iterator().next();
                                data.put("catalystItem", catalyst.save(new CompoundTag()));
                            }
                            
                            // 保存输出物品信息
                            CompoundTag outputs = new CompoundTag();
                            int i = 0;
                            for (ItemStack output : recipe.getOutputs()) {
                                outputs.put("output_" + i, output.save(new CompoundTag()));
                                i++;
                            }
                            data.put("outputItems", outputs);
                        } else {
                            data.putBoolean("hasRecipe", false);
                        }
                    });
        }
    }
}
