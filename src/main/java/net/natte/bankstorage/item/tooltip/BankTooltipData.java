package net.natte.bankstorage.item.tooltip;

import java.util.List;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;

public record BankTooltipData(List<ItemStack> items) implements TooltipData {}