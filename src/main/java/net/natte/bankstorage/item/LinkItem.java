package net.natte.bankstorage.item;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.util.Util;

public class LinkItem extends BankFunctionality implements DyeableItem {

    public static final String BANK_TYPE_KEY = "bank:link_type";

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    public LinkItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        if (Util.isShiftDown.get()) {
            if (Util.hasUUID(stack))
                tooltip.add(Text.literal(Util.getUUID(stack).toString()).formatted(Formatting.DARK_AQUA));
        }

        BankType type = getType(stack);
        Text formattedStackLimit = Text.literal(NUMBER_FORMAT.format(type.stackLimit));
        tooltip.add(Text.translatable("tooltip.bankstorage.stacklimit", formattedStackLimit));
        tooltip.add(Text.translatable("tooltip.bankstorage.numslots", Text.literal(String.valueOf(type.size()))));
        super.appendTooltip(stack, world, tooltip, context);
    }

    public static BankType getType(ItemStack stack) {
        return BankItemStorage.getBankTypeFromName(getTypeName(stack));
    }

    public static String getTypeName(ItemStack itemStack) {
        NbtCompound nbt = itemStack.getOrCreateNbt();
        if (nbt.contains(BANK_TYPE_KEY)) {
            return nbt.getString(BANK_TYPE_KEY);
        } else {
            return "bank_1";
        }
    }

    public static void setTypeName(ItemStack itemStack, String name) {
        NbtCompound nbt = itemStack.getOrCreateNbt();
        nbt.putString(BANK_TYPE_KEY, name);
    }
}
