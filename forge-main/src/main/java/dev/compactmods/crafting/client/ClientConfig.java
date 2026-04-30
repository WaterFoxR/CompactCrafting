package dev.compactmods.crafting.client;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.FastColor;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {

    public static ForgeConfigSpec CONFIG;

    private static ForgeConfigSpec.ConfigValue<String> PROJECTOR_COLOR;
    private static ForgeConfigSpec.ConfigValue<String> PROJECTOR_OFF_COLOR;
    private static ForgeConfigSpec.IntValue PLACEMENT_TIME;

    public static ForgeConfigSpec.BooleanValue ENABLE_DEBUG_ON_F3;

    public static int projectorColor = FastColor.ARGB32.color(255, 255, 106, 0);
    public static int projectorOffColor = FastColor.ARGB32.color(255, 137, 137, 137);
    public static int placementTime = 160;

    static {
        generateConfig();
    }

    public static boolean doDebugRender() {
        return Minecraft.getInstance().options.renderDebug && ENABLE_DEBUG_ON_F3.get();
    }

    private static void generateConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder
                .comment("Projector Settings")
                .push("projectors");

        PROJECTOR_COLOR = builder
                .comment(
                        "The color for the projector fields. (HEX format)",
                        "Examples: Orange - #FF6A00, Violet - #32174D, Green - #00A658, Blue - #3A7FE1"
                )
                .define("projectorColor", "#FF6A00");

        PROJECTOR_OFF_COLOR = builder
                .comment("The color for the projectors when not active. (HEX format)")
                .define("projectorOffColor", "#898989");

        ENABLE_DEBUG_ON_F3 = builder
                .comment("Whether or not activating F3 will enable debug renderers.")
                .define("projectorDebugger", false);

        PLACEMENT_TIME = builder
                .comment("How long (ticks) the placement helper will show on right-clicking a projector.")
                .defineInRange("placementTime", 160, 60, 240);

        builder.pop();

        CONFIG = builder.build();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Reloading configEvent) {
        final var c = configEvent.getConfig();
        if(c.getModId().equals(CompactCrafting.MOD_ID) && c.getType().equals(ModConfig.Type.CLIENT)) {
            projectorColor = extractHexColor(PROJECTOR_COLOR.get(), 0x00FF6A00);
            projectorOffColor = extractHexColor(PROJECTOR_OFF_COLOR.get(), 0x00898989);
            placementTime = PLACEMENT_TIME.get();
        }
    }

    private static int extractHexColor(String hex, int def) {
        try {
            // 检查是否以 # 开头
            if (!hex.startsWith("#")) {
                return def;
            }
            
            // 解析颜色
            int color = Integer.parseInt(hex.substring(1), 16);
            
            // 确保有 alpha 通道 (255 = 完全不透明)
            int red = FastColor.ARGB32.red(color);
            int green = FastColor.ARGB32.green(color);
            int blue = FastColor.ARGB32.blue(color);
            return FastColor.ARGB32.color(255, red, green, blue);
        } catch (NumberFormatException nfe) {
            CompactCrafting.LOGGER.warn("Bad config value for projector color: {}", hex);
            return def;
        }
    }
}
