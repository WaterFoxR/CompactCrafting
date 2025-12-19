package dev.compactmods.crafting.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

@SuppressWarnings("deprecation")
public class CodecExtensions {

     public static final Codec<Block> BLOCK_ID_CODEC = ResourceLocation.CODEC
             .flatXmap(rl -> ForgeRegistries.BLOCKS.containsKey(rl) ?
                             DataResult.success(ForgeRegistries.BLOCKS.getValue(rl)) :
                             DataResult.error(() -> String.format("Block %s is not registered.", rl)),
                     bl -> DataResult.success(ForgeRegistries.BLOCKS.getKey(bl)))
             .stable();

    // 创建一个可以处理字符串形式NBT的编解码器
    private static final Codec<CompoundTag> STRINGABLE_COMPOUND_TAG_CODEC = Codec.STRING.flatXmap(
            str -> {
                try {
                    return DataResult.success(TagParser.parseTag(str));
                } catch (Exception e) {
                    return DataResult.error(() -> "Failed to parse CompoundTag from string: " + str);
                }
            },
            tag -> DataResult.success(tag.toString())
    );

    public static final Codec<ItemStack> FRIENDLY_ITEMSTACK = RecordCodecBuilder.create(i -> i.group(
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("id").forGetter(is -> is.isEmpty() ? Optional.empty() : Optional.of(is.getItem())),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("item").forGetter(is -> is.isEmpty() ? Optional.empty() : Optional.of(is.getItem())),
            Codec.INT.optionalFieldOf("Count", 1).forGetter(ItemStack::getCount),
            Codec.INT.optionalFieldOf("count", 1).forGetter(ItemStack::getCount),
            STRINGABLE_COMPOUND_TAG_CODEC.optionalFieldOf("tag").forGetter(is -> Optional.ofNullable(is.getTag())),
            STRINGABLE_COMPOUND_TAG_CODEC.optionalFieldOf("nbt").forGetter(is -> Optional.ofNullable(is.getTag()))
    ).apply(i, (id, item, Count, count, tag, nbt) -> {
        // 优先使用"id"字段，如果不存在则使用"item"字段
        var itemId = id.orElse(item.orElse(null));
        if (itemId != null) {
            // 优先使用"Count"字段，如果不存在则使用"count"字段
            int stackSize = Math.max(Count, count);
            ItemStack is = new ItemStack(itemId, stackSize);

            if(tag.isPresent()){
                is.setTag(tag.get());
                return is;
            }
            if(nbt.isPresent()){
                is.setTag(nbt.get());
                return is;
            }
            return new ItemStack(itemId, stackSize);
        }
        return new ItemStack(Items.BARRIER,1);
    }));
}
