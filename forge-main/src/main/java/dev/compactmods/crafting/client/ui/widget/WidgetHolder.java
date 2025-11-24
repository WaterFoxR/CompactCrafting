package dev.compactmods.crafting.client.ui.widget;

import dev.compactmods.crafting.client.ui.widget.renderable.IWidgetPostBackgroundRenderable;
import dev.compactmods.crafting.client.ui.widget.renderable.IWidgetPreBackgroundRenderable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WidgetHolder implements Renderable {
    protected List<WidgetBase> widgets;

    public WidgetHolder() {
        this.widgets = new ArrayList<>();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        for(WidgetBase w : widgets) {
            w.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    public void add(WidgetBase widget) {
        this.widgets.add(widget);
    }

    public void renderPreBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        for (WidgetBase w : this.widgets) {
            if (w instanceof IWidgetPreBackgroundRenderable)
                ((IWidgetPreBackgroundRenderable) w).renderPreBackground(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    public void renderPostBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        for (WidgetBase w : this.widgets) {
            if (w instanceof IWidgetPostBackgroundRenderable)
                ((IWidgetPostBackgroundRenderable) w).renderPostBackground(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    public List<WidgetBase> getWidgets() {
        return this.widgets;
    }
}