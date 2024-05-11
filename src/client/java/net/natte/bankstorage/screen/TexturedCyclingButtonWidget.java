package net.natte.bankstorage.screen;

import java.time.Duration;
import java.util.function.Consumer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TexturedCyclingButtonWidget<T extends CycleableOption> extends ButtonWidget {

    public T state;

    private static final int textureWidth = 256;
    private static final int textureHeight = 256;

    private Identifier texture;
    private int hoveredVOffset;

    @SuppressWarnings("unchecked") // cast to TexturedCyclingButtonWidget<T> line 27,
    public TexturedCyclingButtonWidget(T state, int x, int y, int width, int height, int hoveredVOffset,
            Identifier texture,
            Consumer<TexturedCyclingButtonWidget<T>> pressAction) {
        super(x, y, width, height, ScreenTexts.EMPTY, 
                button -> pressAction.accept((TexturedCyclingButtonWidget<T>) button), DEFAULT_NARRATION_SUPPLIER);

        this.texture = texture;
        this.hoveredVOffset = hoveredVOffset;

        this.state = state;
        this.refreshTooltip();
        this.setTooltipDelay(Duration.ofMillis(700));
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawGuiTexture(this.texture, this.getX(), this.getY(), this.state.uOffset(), this.state.vOffset(),
                this.hoveredVOffset,
                this.width, this.height, textureWidth, textureHeight);
    }

    public void refreshTooltip() {
        this.setTooltip(state.getTooltip());
    }

}
