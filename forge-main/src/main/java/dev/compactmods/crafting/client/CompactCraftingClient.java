package dev.compactmods.crafting.client;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.client.render.MiniaturizationFieldRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT)
public class CompactCraftingClient {

    public CompactCraftingClient(IEventBus modBus) {
        modBus.addListener(MiniaturizationFieldRenderer::onRenderStage);
    }

}