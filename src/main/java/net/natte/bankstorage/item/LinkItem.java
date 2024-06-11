package net.natte.bankstorage.item;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.util.Util;

public class LinkItem extends BankFunctionality {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    public LinkItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        if (Util.isShiftDown.get()) {
            if (Util.hasUUID(stack))
                tooltip.add(Component.literal(Util.getUUID(stack).toString()).withStyle(ChatFormatting.DARK_AQUA));
        }

        BankType type = Util.getType(stack);
        Component formattedStackLimit = Component.literal(NUMBER_FORMAT.format(type.stackLimit));
        tooltip.add(Component.translatable("tooltip.bankstorage.stacklimit", formattedStackLimit));
        tooltip.add(Component.translatable("tooltip.bankstorage.numslots", Component.literal(String.valueOf(type.size()))));
        super.appendHoverText(stack, context, tooltip,
                tooltipFlag);
    }

}
