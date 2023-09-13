package net.natte.bankstorage.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;

public class BankScreen extends HandledScreen<BankScreenHandler> {


    private static final Identifier TEXTURE = new Identifier(BankStorage.MOD_ID, "textures/gui/bank_1.png");

    public BankScreen(BankScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
        titleY += 34;
    }

    @Override
    protected void drawBackground(DrawContext drawContext, float timeDelta, int mouseX, int mouseY) {
        renderBackground(drawContext);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2 + 34;
        drawContext.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
        

    }

    

    
    
}
