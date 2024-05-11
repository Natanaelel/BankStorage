package net.natte.bankstorage.screen;

import java.time.Duration;
import java.util.function.Consumer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.natte.bankstorage.options.SortMode;

public class SortButtonWidget extends ButtonWidget {

    public SortMode sortMode;
    private long lastPressedTime;
    private Identifier texture;

    public SortButtonWidget(SortMode sortMode, int x, int y, int width, int height, Identifier texture,
            Consumer<SortButtonWidget> pressAction) {
        super(x, y, width, height, ScreenTexts.EMPTY, button -> pressAction.accept((SortButtonWidget) button), DEFAULT_NARRATION_SUPPLIER);
        this.sortMode = sortMode;
        this.lastPressedTime = 0;
        this.texture = texture;

        this.refreshTooltip();
        this.setTooltipDelay(Duration.ofMillis(700));
    }

    @Override
    public void onPress() {
        super.onPress();
        this.lastPressedTime = Util.getMeasuringTimeMs();
    }

    public long timeSinceLastPressed() {
        return Util.getMeasuringTimeMs() - this.lastPressedTime;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(this.texture, this.getX(), this.getY(), uOffset(), vOffset() + (this.isSelected() ? this.height : 0),
                
                this.width, this.height, 256, 256);
    }

    private int uOffset() {
        return switch (sortMode) {
            case COUNT -> 0;
            case NAME -> 14;
            case MOD -> 28;
        };
    }

    private int vOffset() {
        return 98;
    }

    public void refreshTooltip() {
        this.setTooltip(Tooltip.of(Text.translatable("title.bankstorage.sortmode." + sortMode.name().toLowerCase())
                .append(Text.empty().append("\n").append(
                        Text.translatable("tooltip.bankstorage.sortmode." + sortMode.name().toLowerCase())
                                .formatted(Formatting.DARK_GRAY)))));
    }

}
