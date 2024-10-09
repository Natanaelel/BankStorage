package net.natte.bankstorage.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.natte.bankstorage.options.SortMode;

import java.time.Duration;
import java.util.function.Consumer;

public class SortButtonWidget extends Button {

    public SortMode sortMode;
    private long lastPressedTime;
    private final ResourceLocation texture;
    private int uOffset;

    public SortButtonWidget(SortMode sortMode, int x, int y, int width, int height, ResourceLocation texture,
                            Consumer<SortButtonWidget> pressAction) {
        super(x, y, width, height, CommonComponents.EMPTY, button -> pressAction.accept((SortButtonWidget) button),
                DEFAULT_NARRATION);
        this.sortMode = sortMode;
        this.lastPressedTime = 0;
        this.texture = texture;

        this.refreshTooltip();
        this.setTooltipDelay(Duration.ofMillis(700));
    }

    @Override
    public void onPress() {
        super.onPress();
        this.uOffset = getUOffset();
        this.lastPressedTime = Util.getMillis();
    }

    public long timeSinceLastPressed() {
        return Util.getMillis() - this.lastPressedTime;
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.blit(this.texture, this.getX(), this.getY(),
                uOffset, 98 + (this.isHoveredOrFocused() ? this.height : 0),
                this.width, this.height, 256, 256);
    }

    private int getUOffset() {
        return switch (sortMode) {
            case COUNT -> 0;
            case NAME -> 14;
            case MOD -> 28;
        };
    }

    public void refreshTooltip() {
        this.setTooltip(Tooltip.create(Component.translatable("title.bankstorage.sortmode." + sortMode.name().toLowerCase())
                .append(Component.empty().append("\n").append(
                        Component.translatable("tooltip.bankstorage.sortmode." + sortMode.name().toLowerCase())
                                .withStyle(ChatFormatting.DARK_GRAY)))));
    }
}
