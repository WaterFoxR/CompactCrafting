package dev.compactmods.crafting.network;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel MAIN_CHANNEL = NetworkRegistry.newSimpleChannel(
            CompactCrafting.modRL("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void initialize() {
        MAIN_CHANNEL.messageBuilder(FieldActivatedPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FieldActivatedPacket::encode)
                .decoder(FieldActivatedPacket::new)
                .consumerMainThread(FieldActivatedPacket::handle)
                .add();

        MAIN_CHANNEL.messageBuilder(FieldDeactivatedPacket.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FieldDeactivatedPacket::encode)
                .decoder(FieldDeactivatedPacket::new)
                .consumerMainThread(FieldDeactivatedPacket::handle)
                .add();

        MAIN_CHANNEL.messageBuilder(ClientFieldWatchPacket.class, 3, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientFieldWatchPacket::encode)
                .decoder(ClientFieldWatchPacket::fromBuffer)
                .consumerMainThread(ClientFieldWatchPacket::handle)
                .add();

        MAIN_CHANNEL.messageBuilder(ClientFieldUnwatchPacket.class, 4, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientFieldUnwatchPacket::encode)
                .decoder(ClientFieldUnwatchPacket::new)
                .consumerMainThread(ClientFieldUnwatchPacket::handle)
                .add();

        MAIN_CHANNEL.messageBuilder(FieldRecipeChangedPacket.class, 5, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FieldRecipeChangedPacket::encode)
                .decoder(FieldRecipeChangedPacket::fromBuffer)
                .consumerMainThread(FieldRecipeChangedPacket::handle)
                .add();
    }
}
