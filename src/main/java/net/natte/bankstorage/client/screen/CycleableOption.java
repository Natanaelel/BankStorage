package net.natte.bankstorage.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public interface CycleableOption {

    public Component getName();
    public Component getInfo();

    public int uOffset();
    public int vOffset();

    default public Tooltip getTooltip() {
        return Tooltip.create(getName().copy().append(Component.empty().append("\n").append(getInfo()).withStyle(ChatFormatting.DARK_GRAY)));
    }
}
