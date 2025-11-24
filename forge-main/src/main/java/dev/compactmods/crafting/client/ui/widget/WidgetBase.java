package dev.compactmods.crafting.client.ui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;

public abstract class WidgetBase implements Renderable, GuiEventListener {
    protected final int width;
    protected final int height;

    protected WidgetBase(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public abstract void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks);
}
