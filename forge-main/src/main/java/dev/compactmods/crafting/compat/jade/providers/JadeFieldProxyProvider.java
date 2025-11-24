package dev.compactmods.crafting.compat.jade.providers;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.core.CCCapabilities;
import dev.compactmods.crafting.proxies.block.FieldProxyBlock;
import dev.compactmods.crafting.proxies.data.BaseFieldProxyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum JadeFieldProxyProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(CompactCrafting.MOD_ID, "field_proxy");
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlock() instanceof FieldProxyBlock))
            return;

        String fieldCenter = accessor.getServerData().getString("fieldCenter");
        tooltip.add(Component.translatable("tooltip.compactcrafting.proxy_bound", fieldCenter));
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlock() instanceof FieldProxyBlock))
            return;

        BaseFieldProxyEntity tile = (BaseFieldProxyEntity) accessor.getLevel().getBlockEntity(accessor.getPosition());
        if(tile == null)
            return;

        tile.getCapability(CCCapabilities.MINIATURIZATION_FIELD)
                .ifPresent(field -> {
                    BlockPos fieldCenter = field.getCenter();
                    data.putString("fieldCenter", fieldCenter.toString());
                });
    }
}
