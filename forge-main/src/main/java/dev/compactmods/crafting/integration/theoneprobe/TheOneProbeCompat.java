package dev.compactmods.crafting.integration.theoneprobe;

import net.minecraftforge.fml.InterModComms;

public class TheOneProbeCompat {


    public static void sendIMC() {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", TOPMain::new);
    }
}
