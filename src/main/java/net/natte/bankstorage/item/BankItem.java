package net.natte.bankstorage.item;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.util.Util;

public class BankItem extends BankFunctionality {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    private final BankType type;

    public BankItem(Item.Properties settings, BankType type) {
        super(settings);
        this.type = type;
    }

    public BankType getType() {
        return this.type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipType) {
        if (Util.isShiftDown.get()) {
            if (Util.hasUUID(stack))
                tooltip.add(Component.literal(Util.getUUID(stack).toString()).withStyle(ChatFormatting.DARK_AQUA));
        }
        Component formattedStackLimit = Component.literal(NUMBER_FORMAT.format(this.type.stackLimit));
        tooltip.add(Component.translatable("tooltip.bankstorage.stacklimit", formattedStackLimit));
        tooltip.add(Component.translatable("tooltip.bankstorage.numslots", Component.literal(String.valueOf(this.type.size()))));
        super.appendHoverText(stack, context, tooltip, tooltipType);
    }
}
