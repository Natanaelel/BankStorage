package net.natte.bankstorage.item;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.util.Util;

public class BankItem extends BankFunctionality {

    public static final String UUID_KEY = "bank:uuid";

    private BankType type;

    public BankItem(Settings settings, BankType type) {
        super(settings);
        this.type = type;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        return super.use(world, player, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return super.useOnBlock(context);
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
        tooltip.add(Text.translatable("tooptip.bankstorage.stacklimit", Text.literal(NumberFormat.getNumberInstance(Locale.US).format(64 * this.type.slotStorageMultiplier))));
        tooltip.add(Text.translatable("tooptip.bankstorage.numslots", Text.literal("" + this.type.size())));
        super.appendTooltip(stack, world, tooltip, context);
    }

}
