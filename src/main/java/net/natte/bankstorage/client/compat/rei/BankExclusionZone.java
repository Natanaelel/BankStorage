package net.natte.bankstorage.client.compat.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZonesProvider;
import net.natte.bankstorage.client.screen.BankScreen;

import java.util.Collection;
import java.util.List;

public class BankExclusionZone implements ExclusionZonesProvider<BankScreen> {
    @Override
    public Collection<Rectangle> provide(BankScreen screen) {
        if (screen.hasScrollBar()) {
            return List.of(new Rectangle(screen.getGuiLeft() + 176, screen.getGuiTop(), 18, screen.getScrollBarHeight()));
        }
        return List.of();
    }
}
