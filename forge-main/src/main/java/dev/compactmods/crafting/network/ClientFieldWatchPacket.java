package dev.compactmods.crafting.network;

import java.util.function.Supplier;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.client.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record ClientFieldWatchPacket(CompoundTag clientData) {

    public static ClientFieldWatchPacket fromField(IMiniaturizationField field) {
        return new ClientFieldWatchPacket(field.clientData());
    }

    public static ClientFieldWatchPacket fromBuffer(FriendlyByteBuf buf) {
        return new ClientFieldWatchPacket(buf.readAnySizeNbt());
    }

    public static void encode(ClientFieldWatchPacket pkt, FriendlyByteBuf buf) {
        buf.writeNbt(pkt.clientData());
    }

    public static boolean handle(ClientFieldWatchPacket pkt, Supplier<NetworkEvent.Context> context) {
        ClientPacketHandler.handleFieldData(pkt.clientData);
        return true;
    }
}
