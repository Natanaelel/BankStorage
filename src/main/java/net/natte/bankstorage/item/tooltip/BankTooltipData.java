package net.natte.bankstorage.item.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record BankTooltipData(List<ItemStack> items) implements TooltipComponent {}
