package net.natte.bankstorage.item;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.item.TooltipType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.util.Util;

public class LinkItem extends BankFunctionality {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    public LinkItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType tooltipType) {
        if (Util.isShiftDown.get()) {
            if (Util.hasUUID(stack))
                tooltip.add(Text.literal(Util.getUUID(stack).toString()).formatted(Formatting.DARK_AQUA));
        }

        BankType type = getType(stack);
        Text formattedStackLimit = Text.literal(NUMBER_FORMAT.format(type.stackLimit));
        tooltip.add(Text.translatable("tooptip.bankstorage.stacklimit", formattedStackLimit));
        tooltip.add(Text.translatable("tooptip.bankstorage.numslots", Text.literal(String.valueOf(type.size()))));
        super.appendTooltip(stack, context, tooltip, tooltipType);
    }

    public static BankType getType(ItemStack stack) {
        return stack.getOrDefault(BankStorage.BankTypeComponentType, BankStorage.bankTypes.get(0));
    }

    public static void setType(ItemStack itemStack, BankType type) {
        itemStack.set(BankStorage.BankTypeComponentType, type);
    }
}
