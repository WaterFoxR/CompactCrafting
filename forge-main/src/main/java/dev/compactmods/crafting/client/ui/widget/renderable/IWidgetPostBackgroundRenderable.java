package dev.compactmods.crafting.client.ui.widget.renderable;

import net.minecraft.client.gui.GuiGraphics;

public interface IWidgetPostBackgroundRenderable {
    void renderPostBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks);
}