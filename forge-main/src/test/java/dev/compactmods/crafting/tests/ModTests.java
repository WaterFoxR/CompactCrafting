package dev.compactmods.crafting.tests;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class ModTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void canCreateItemGroup(final GameTestHelper test) {
        try {
            ItemStack icon = CompactCrafting.ITEM_GROUP.get().getIconItem();
            test.succeed();
        }

        catch(Exception e) {
            test.fail(e.getMessage());
        }
    }
}
