package dev.compactmods.crafting;

import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.client.ui.container.ContainerRegistration;
import dev.compactmods.crafting.core.*;
import dev.compactmods.crafting.network.NetworkHandler;
import dev.compactmods.crafting.recipes.components.ComponentRegistration;
import dev.compactmods.crafting.server.ServerConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CompactCrafting.MOD_ID)
public class CompactCrafting
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger(CompactCrafting.MOD_ID);
    public static final Logger RECIPE_LOGGER = LogManager.getLogger("CCRecipeLoader");

    public static final String MOD_ID = "compactcrafting";

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<CreativeModeTab> ITEM_GROUP = CREATIVE_MODE_TABS.register("compactcrafting", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.compactcrafting"))
                    .icon(() -> new ItemStack(CCItems.FIELD_PROJECTOR_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(CCItems.FIELD_PROJECTOR_ITEM.get());
                        output.accept(CCItems.PROJECTOR_DISH_ITEM.get());
                        output.accept(CCItems.BASE_ITEM.get());
                        output.accept(CCItems.RESCAN_PROXY_ITEM.get());
                        output.accept(CCItems.MATCH_PROXY_ITEM.get());
                    })
                    .build());

    public CompactCrafting() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(this::setup);

        ModLoadingContext mlCtx = ModLoadingContext.get();
        mlCtx.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);
        mlCtx.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);

        CCBlocks.init(modBus);
        CCCatalystTypes.init(modBus);
        CCItems.init(modBus);
        CCLayerTypes.init(modBus);
        CCMiniaturizationRecipes.init(modBus);
        CREATIVE_MODE_TABS.register(modBus);

        ComponentRegistration.init(modBus);
        ContainerRegistration.init(modBus);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        NetworkHandler.initialize();
    }
}
