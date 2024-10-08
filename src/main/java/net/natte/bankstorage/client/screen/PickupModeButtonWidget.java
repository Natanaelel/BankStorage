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
public class PickupModeButtonWidget extends Button {

    PickupMode pickupMode;

    private static final int textureWidth = 256;
    private static final int textureHeight = 256;

    private final ResourceLocation texture;
    private int uOffset;

    public PickupModeButtonWidget(PickupMode pickupMode, int x, int y, int width, int height,
                           ResourceLocation texture,
                           Consumer<PickupModeButtonWidget> pressAction) {
        super(x, y, width, height, CommonComponents.EMPTY,
                button -> pressAction.accept((PickupModeButtonWidget) button), DEFAULT_NARRATION);

        this.texture = texture;

        this.pickupMode = pickupMode;
        this.refreshTooltip();
        updateUOffset();
        this.setTooltipDelay(Duration.ofMillis(700));
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.blit(this.texture,
                this.getX(), this.getY(), 0,
                this.uOffset, 70 + (this.isHoveredOrFocused() ? this.height : 0),
                this.width, this.height, textureWidth, textureHeight);
    }

    public void refreshTooltip() {
        String name = switch (this.pickupMode) {
            case NONE -> "no_pickup";
            case ALL -> "pickup_all";
            case FILTERED -> "filtered";
            case VOID -> "void_overflow";
        };

        this.setTooltip(
                Tooltip.create(
                        Component.translatable("title.bankstorage.pickupmode." + name)
                                .append("\n")
                                .append(
                                        Component.translatable("tooltip.bankstorage.pickupmode." + name)
                                                .withStyle(ChatFormatting.DARK_GRAY)
                                )));
    }


    public void nextState() {
        this.pickupMode = this.pickupMode.next();
        updateUOffset();
    }
    private void updateUOffset(){
        this.uOffset = switch (this.pickupMode) {
            case NONE -> 0;
            case ALL -> 14;
            case FILTERED -> 28;
            case VOID -> 42;
        };
    }
}
