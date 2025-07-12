package net.natte.bankstorage.item;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.util.Util;

public class BankItem extends BankFunctionality implements DyeableItem {

    public static final String UUID_KEY = "bank:uuid";
    public static final String OPTIONS_KEY = "bank:options";

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    private BankType type;

    public BankItem(Settings settings, BankType type) {
        super(settings);
        this.type = type;
    }

    public BankType getType() {
        return this.type;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        if (Util.isShiftDown.get()) {
            if (Util.hasUUID(stack))
                tooltip.add(Text.literal(Util.getUUID(stack).toString()).formatted(Formatting.DARK_AQUA));
        }
        Text formattedStackLimit = Text.literal(NUMBER_FORMAT.format(this.type.stackLimit));
        tooltip.add(Text.translatable("tooltip.bankstorage.stacklimit", formattedStackLimit));
        tooltip.add(Text.translatable("tooltip.bankstorage.numslots", Text.literal(String.valueOf(this.type.size()))));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
