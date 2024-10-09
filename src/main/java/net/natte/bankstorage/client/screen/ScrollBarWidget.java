package net.natte.bankstorage.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.natte.bankstorage.util.Util;

import java.util.function.Consumer;

public class ScrollBarWidget implements Renderable, GuiEventListener, NarratableEntry {

    private static final ResourceLocation WIDGETS_TEXTURE = Util.ID("textures/gui/widgets.png");

    private final int x;
    private final int y;
    private final int totalRows;
    private final int visibleRows;
    private final int scrollBarHeight;
    private int scrollerY;
    private final Consumer<Float> onScroll;
    private boolean isScrolling = false;

    public ScrollBarWidget(int x, int y, int totalRows, int visibleRows, float initialValue, Consumer<Float> onScroll) {
        this.x = x;
        this.y = y;

        this.totalRows = totalRows;
        this.visibleRows = visibleRows;
        this.onScroll = onScroll;
        this.scrollBarHeight = this.visibleRows * 18;

        this.scrollerY = getPosition(initialValue);
    }

    private int getPosition(float value) {
        int scrollerMin = y + 26;
        int scrollerMax = y + 9 + scrollBarHeight;
        return Mth.clamp((int) Mth.lerp(value, scrollerMin, scrollerMax), y + 26, y + 9 + scrollBarHeight);
    }

    private float getValue(int position) {
        int scrollerMin = y + 26;
        int scrollerMax = y + 9 + scrollBarHeight;
        return Mth.inverseLerp(position, scrollerMin, scrollerMax);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        guiGraphics.blit(WIDGETS_TEXTURE, x, y - 2, 0, 151, 24, 20, 256, 256);
        guiGraphics.blit(WIDGETS_TEXTURE, x, y + 18, 24, scrollBarHeight - 2, 0, 172, 24, 8, 256, 256);
        guiGraphics.blit(WIDGETS_TEXTURE, x, y + 16 + scrollBarHeight, 0, 181, 24, 10, 256, 256);

        guiGraphics.blit(WIDGETS_TEXTURE, x + 2, scrollerY - 8, 0, 192, 12, 15, 256, 256);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isOverScrollArea(mouseX, mouseY)) {
            this.isScrolling = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isScrolling) {
            this.isScrolling = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isScrolling) {
            scrollerY = (int) Mth.clamp(mouseY, y + 26, y + 9 + scrollBarHeight);
            this.onScroll.accept(getValue(scrollerY));
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isOverScrollArea(mouseX, mouseY)) {
            int hiddenRows = this.totalRows - this.visibleRows;
            int topRow = Mth.clamp(Math.round(getValue(scrollerY) * hiddenRows), 0, hiddenRows);
            topRow -= (int) scrollY;
            topRow = Mth.clamp(topRow, 0, hiddenRows);
            this.scrollerY = getPosition(topRow / (float) hiddenRows);
            this.onScroll.accept(getValue(scrollerY));

            return true;
        }
        return false;
    }

    public boolean isOverScrollArea(double mouseX, double mouseY) {
        return mouseX >= x + 2 && mouseX <= x + 14 && mouseY >= y + 18 && mouseY <= y + 16 + scrollBarHeight;
    }

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
}
