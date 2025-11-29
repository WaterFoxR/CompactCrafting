package dev.compactmods.crafting.network;

import javax.annotation.Nullable;
import java.util.function.Supplier;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

public record FieldRecipeChangedPacket(BlockPos fieldCenter, @Nullable ResourceLocation recipe) {

    public static FieldRecipeChangedPacket fromField(IMiniaturizationField field) {
        return new FieldRecipeChangedPacket(
                field.getCenter(),
                field.getCurrentRecipe().map(IMiniaturizationRecipe::getRecipeIdentifier).orElse(null)
        );
    }

    public static FieldRecipeChangedPacket fromBuffer(FriendlyByteBuf buf) {
        BlockPos center = buf.readBlockPos();
        ResourceLocation recipe = buf.readBoolean() ? ResourceLocation.tryParse(buf.readUtf()) : null;
        return new FieldRecipeChangedPacket(center, recipe);
    }

    public static void encode(FieldRecipeChangedPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.fieldCenter);
        buf.writeBoolean(pkt.recipe != null);
        if(pkt.recipe != null)
            buf.writeUtf(pkt.recipe.toString());
    }

    public static boolean handle(FieldRecipeChangedPacket pkt, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientPacketHandler.handleRecipeChanged(pkt.fieldCenter, pkt.recipe));
        return true;
    }
}
