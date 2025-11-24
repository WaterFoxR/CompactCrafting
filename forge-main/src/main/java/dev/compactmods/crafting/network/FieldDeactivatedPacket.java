package dev.compactmods.crafting.network;

import java.util.function.Supplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class FieldDeactivatedPacket {

    protected static final Codec<FieldDeactivatedPacket> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.xmap(MiniaturizationFieldSize::valueOf, Enum::name)
                    .fieldOf("size").forGetter(x -> x.fieldSize),
            BlockPos.CODEC.fieldOf("center").forGetter(x -> x.fieldCenter)
    ).apply(i, FieldDeactivatedPacket::new));

    private final MiniaturizationFieldSize fieldSize;
    private final BlockPos fieldCenter;
    private final BlockPos[] projectors;

    public FieldDeactivatedPacket(MiniaturizationFieldSize fieldSize, BlockPos fieldCenter) {
        this.fieldSize = fieldSize;
        this.fieldCenter = fieldCenter;

        this.projectors = fieldSize.getProjectorLocations(fieldCenter)
                .map(BlockPos::immutable).toArray(BlockPos[]::new);
    }

    public FieldDeactivatedPacket(FriendlyByteBuf buf) {
        this.fieldSize = MiniaturizationFieldSize.valueOf(buf.readUtf());
        this.fieldCenter = buf.readBlockPos();

        this.projectors = fieldSize.getProjectorLocations(fieldCenter)
                .map(BlockPos::immutable).toArray(BlockPos[]::new);
    }

    public static boolean handle(FieldDeactivatedPacket message, Supplier<NetworkEvent.Context> context) {
//        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
//            ClientPacketHandler.handleFieldDeactivation(message.fieldCenter);
//        });
//
//        return true;
        NetworkEvent.Context ctx = context.get();

        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientPacketHandler.handleFieldDeactivation(message.fieldCenter);
        }));

        ctx.setPacketHandled(true);
        return true;
    }

    public static void encode(FieldDeactivatedPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.fieldSize.name());
        buf.writeBlockPos(pkt.fieldCenter);
    }
}
