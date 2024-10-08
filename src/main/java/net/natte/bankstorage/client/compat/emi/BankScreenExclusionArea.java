package net.natte.bankstorage.client.compat.emi;

import dev.emi.emi.api.EmiExclusionArea;
import dev.emi.emi.api.widget.Bounds;
import net.natte.bankstorage.client.screen.BankScreen;

import java.util.function.Consumer;

public class BankScreenExclusionArea implements EmiExclusionArea<BankScreen> {
    @Override
    public void addExclusionArea(BankScreen screen, Consumer<Bounds> bounds) {
        if (screen.hasScrollBar()) {
            bounds.accept(new Bounds(screen.getGuiLeft() + 176, screen.getGuiTop(), 18, screen.getScrollBarHeight()));
        }
    }
}
