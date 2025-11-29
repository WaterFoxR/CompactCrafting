package dev.compactmods.crafting.datagen;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.core.CCItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class LootTableGenerator extends LootTableProvider {

    public LootTableGenerator(PackOutput packOutput) {
        super(packOutput, ImmutableSet.of(), ImmutableList.of());
    }

    @Override
    public List<SubProviderEntry> getTables() {
        return ImmutableList.of(new SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK));
    }


    @Override
    public void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
        for (Map.Entry<ResourceLocation, LootTable> entry : map.entrySet()) {
            ResourceLocation name = entry.getKey();
            LootTable table = entry.getValue();
            table.validate(validationtracker);
//            LootTables.validate(validationtracker, name, table);
        }
    }

    private static class Blocks implements LootTableSubProvider {
        @Override
        public void generate(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
            registerSelfDroppedBlock(consumer, CCBlocks.FIELD_PROJECTOR_BLOCK, CCItems.FIELD_PROJECTOR_ITEM);
            registerSelfDroppedBlock(consumer, CCBlocks.MATCH_FIELD_PROXY_BLOCK, CCItems.MATCH_PROXY_ITEM);
            registerSelfDroppedBlock(consumer, CCBlocks.RESCAN_FIELD_PROXY_BLOCK, CCItems.RESCAN_PROXY_ITEM);
        }

//        @Override
//        protected void addTables() {
//            registerSelfDroppedBlock(CCBlocks.FIELD_PROJECTOR_BLOCK, CCItems.FIELD_PROJECTOR_ITEM);
//            registerSelfDroppedBlock(CCBlocks.MATCH_FIELD_PROXY_BLOCK, CCItems.MATCH_PROXY_ITEM);
//            registerSelfDroppedBlock(CCBlocks.RESCAN_FIELD_PROXY_BLOCK, CCItems.RESCAN_PROXY_ITEM);
//        }

        private void registerSelfDroppedBlock(BiConsumer<ResourceLocation, LootTable.Builder> consumer, RegistryObject<Block> block, RegistryObject<Item> item) {
            LootPool.Builder builder = LootPool.lootPool()
                    .name(ForgeRegistries.BLOCKS.getKey(block.get()).toString())
                    .setRolls(ConstantValue.exactly(1))
                    .when(ExplosionCondition.survivesExplosion())
                    .add(LootItem.lootTableItem(item.get()));

            LootTable.Builder tableBuilder = LootTable.lootTable().withPool(builder);
            consumer.accept(block.get().getLootTable(), tableBuilder);
        }

//        @Override
//        protected Iterable<Block> getKnownBlocks() {
//            return ImmutableList.of(
//                    CCBlocks.FIELD_PROJECTOR_BLOCK.get(),
//                    CCBlocks.MATCH_FIELD_PROXY_BLOCK.get(),
//                    CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get()
//            );
//        }
    }
}
