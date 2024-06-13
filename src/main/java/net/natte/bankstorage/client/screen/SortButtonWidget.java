package net.natte.bankstorage.client.screen;

import java.time.Duration;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.natte.bankstorage.options.SortMode;

public class SortButtonWidget extends Button {

    public SortMode sortMode;
    private long lastPressedTime;
    private ResourceLocation texture;

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
        this.lastPressedTime = Util.getMillis();
    }

    public long timeSinceLastPressed() {
        return Util.getMillis() - this.lastPressedTime;
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.blit(this.texture, this.getX(), this.getY(),
                uOffset(), vOffset() + (this.isHoveredOrFocused() ? this.height : 0),
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
        this.setTooltip(Tooltip.create(Component.translatable("title.bankstorage.sortmode." + sortMode.name().toLowerCase())
                .append(Component.empty().append("\n").append(
                        Component.translatable("tooltip.bankstorage.sortmode." + sortMode.name().toLowerCase())
                                .withStyle(ChatFormatting.DARK_GRAY)))));
    }
}
