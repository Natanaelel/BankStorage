package net.natte.bankstorage.client.screen;

import java.time.Duration;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.natte.bankstorage.options.PickupMode;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TexturedCyclingButtonWidget extends Button {

    PickupModeOption state;

    private static final int textureWidth = 256;
    private static final int textureHeight = 256;

    private final ResourceLocation texture;

    TexturedCyclingButtonWidget(PickupModeOption state, int x, int y, int width, int height,
            ResourceLocation texture,
            Consumer<TexturedCyclingButtonWidget> pressAction) {
        super(x, y, width, height, CommonComponents.EMPTY,
                button -> pressAction.accept((TexturedCyclingButtonWidget) button), DEFAULT_NARRATION);

        this.texture = texture;

        this.state = state;
        this.refreshTooltip();
        this.setTooltipDelay(Duration.ofMillis(700));
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.blit(this.texture,
                this.getX(), this.getY(), 0,
                this.state.uOffset(), this.state.vOffset() + (this.isHoveredOrFocused() ? this.height : 0),
                this.width, this.height, textureWidth, textureHeight);
    }

    public void refreshTooltip() {
        this.setTooltip(state.getTooltip());
    }

    public void nextState() {
         this.state = this.state.next();
    }
}

@OnlyIn(Dist.CLIENT)
enum PickupModeOption {
    NO_PICKUP("no_pickup", 0, 70),
    ALL("pickup_all", 14, 70),
    FILTERED("filtered", 28, 70),
    VOID_OVERFLOW("void_overflow", 42, 70);

    private final Component name;
    private final Component info;

    private final int uOffset;
    private final int vOffset;

    PickupModeOption(String name, int uOffset, int vOffset) {
        this.name = Component.translatable("title.bankstorage.pickupmode." + name);
        this.info = Component.translatable("tooltip.bankstorage.pickupmode." + name);
        this.uOffset = uOffset;
        this.vOffset = vOffset;
    }

    public static PickupModeOption from(PickupMode pickupMode) {
        return switch (pickupMode) {
            case NONE -> NO_PICKUP;
            case ALL -> ALL;
            case FILTERED -> FILTERED;
            case VOID -> VOID_OVERFLOW;
        };
    }

    public PickupMode toPickupMode() {
        return switch (this) {
            case NO_PICKUP -> PickupMode.NONE;
            case ALL -> PickupMode.ALL;
            case FILTERED -> PickupMode.FILTERED;
            case VOID_OVERFLOW -> PickupMode.VOID;
        };
    }

    public Component getName() {
        return this.name;
    }

    public Component getInfo() {
        return this.info;
    }

    public int uOffset() {
        return this.uOffset;
    }

    public int vOffset() {
        return this.vOffset;
    }

    public Tooltip getTooltip() {
        return Tooltip.create(getName().copy().append(Component.empty().append("\n").append(getInfo()).withStyle(ChatFormatting.DARK_GRAY)));
    }

    public PickupModeOption next() {
        return switch (this) {
            case NO_PICKUP -> ALL;
            case ALL -> FILTERED;
            case FILTERED -> VOID_OVERFLOW;
            case VOID_OVERFLOW -> NO_PICKUP;
        };
    }
}
