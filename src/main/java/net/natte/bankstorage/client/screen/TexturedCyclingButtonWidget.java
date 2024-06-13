package net.natte.bankstorage.client.screen;

import java.time.Duration;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TexturedCyclingButtonWidget<T extends CycleableOption> extends Button {

    public T state;

    private static final int textureWidth = 256;
    private static final int textureHeight = 256;

    private ResourceLocation texture;

    @SuppressWarnings("unchecked") // cast to TexturedCyclingButtonWidget<T> line 27,
    public TexturedCyclingButtonWidget(T state, int x, int y, int width, int height,
            ResourceLocation texture,
            Consumer<TexturedCyclingButtonWidget<T>> pressAction) {
        super(x, y, width, height, CommonComponents.EMPTY,
                button -> pressAction.accept((TexturedCyclingButtonWidget<T>) button), DEFAULT_NARRATION);

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
}
