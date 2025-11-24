package dev.compactmods.crafting.client.ui.widget.renderable;

import net.minecraft.client.gui.GuiGraphics;

public interface IWidgetPreBackgroundRenderable {
    void renderPreBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks);
}