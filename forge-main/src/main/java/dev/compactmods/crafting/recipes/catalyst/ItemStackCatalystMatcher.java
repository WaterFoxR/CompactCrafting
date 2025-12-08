package dev.compactmods.crafting.recipes.catalyst;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.api.catalyst.CatalystType;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;
import dev.compactmods.crafting.core.CCCatalystTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemStackCatalystMatcher implements ICatalystMatcher, CatalystType<ItemStackCatalystMatcher> {

    public static final Codec<ItemStackCatalystMatcher> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter((x) -> ForgeRegistries.ITEMS.getKey(x.item)),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(ItemStackCatalystMatcher::getNbtTag),
            Codec.INT.optionalFieldOf("Count").forGetter(ItemStackCatalystMatcher::getCount)
    ).apply(i, ItemStackCatalystMatcher::new));

    private final Predicate<ItemStack> nbtMatcher;

    private final Item item;
    private final Integer count;
    private final CompoundTag nbt;

    public ItemStackCatalystMatcher() {
        this.item = null;
        this.nbtMatcher = (stack) -> true;
        this.nbt = null;
        this.count = 1;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public ItemStackCatalystMatcher(ResourceLocation item, Optional<CompoundTag> nbt, Optional<Integer> count) {
        this.item = ForgeRegistries.ITEMS.getValue(item);
        this.nbtMatcher = buildMatcher(nbt.orElse(null));
        this.nbt = nbt.orElse(null);
        this.count = count.orElse(1);
    }

    public ItemStackCatalystMatcher(ItemStack stack) {
        this.item = stack.getItem();
        this.nbtMatcher = buildMatcher(stack.getOrCreateTag());
        this.nbt = stack.getTag();
        this.count = stack.getCount();
    }

    private Predicate<ItemStack> buildMatcher(CompoundTag filter) {
        if(filter == null)
            return (stack) -> true;

        return (stack) -> {
            // filters defined but item has no nbt
            if(!stack.hasTag() && !filter.isEmpty()) return false;
            final var tag = stack.getTag();
//            Objects.requireNonNull(tag);
            // 安全检查tag是否为null
            if (tag == null) {
                return filter.isEmpty(); // 如果filter为空，则匹配；否则不匹配
            }
            return NbtUtils.compareNbt(tag, filter, true);
        };
    }

    public boolean matches(ItemStack stack) {
        return stack.getItem().equals(item) && this.nbtMatcher.test(stack);
    }

    private Optional<CompoundTag> getNbtTag() {
        return Optional.ofNullable(nbt);
    }

    public Optional<Integer> getCount() {
        return Optional.of(count);
    }

    @Override
    public CatalystType<?> getType() {
        return CCCatalystTypes.ITEM_STACK_CATALYST.get();
    }

    @Override
    public Set<ItemStack> getPossible() {
        if(nbt == null)
            return Set.of(new ItemStack(item));

        final var withNbt = new ItemStack(item, count);
        withNbt.setTag(nbt);
        return Set.of(withNbt);
    }

    @Override
    public Codec<ItemStackCatalystMatcher> getCodec() {
        return CODEC;
    }
}