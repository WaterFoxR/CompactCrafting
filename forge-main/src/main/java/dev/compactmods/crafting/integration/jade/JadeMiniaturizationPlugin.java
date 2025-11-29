package dev.compactmods.crafting.integration.jade;

import dev.compactmods.crafting.integration.jade.providers.JadeFieldProjectorProvider;
import dev.compactmods.crafting.integration.jade.providers.JadeFieldProxyProvider;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.projector.FieldProjectorEntity;
import dev.compactmods.crafting.proxies.block.MatchFieldProxyBlock;
import dev.compactmods.crafting.proxies.block.RescanFieldProxyBlock;
import dev.compactmods.crafting.proxies.data.MatchFieldProxyEntity;
import dev.compactmods.crafting.proxies.data.RescanFieldProxyEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadeMiniaturizationPlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(JadeFieldProjectorProvider.INSTANCE, FieldProjectorEntity.class);
        registration.registerBlockDataProvider(JadeFieldProxyProvider.INSTANCE, MatchFieldProxyEntity.class);
        registration.registerBlockDataProvider(JadeFieldProxyProvider.INSTANCE, RescanFieldProxyEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(JadeFieldProjectorProvider.INSTANCE, FieldProjectorBlock.class);
        registration.registerBlockComponent(JadeFieldProxyProvider.INSTANCE, MatchFieldProxyBlock.class);
        registration.registerBlockComponent(JadeFieldProxyProvider.INSTANCE, RescanFieldProxyBlock.class);
    }
}