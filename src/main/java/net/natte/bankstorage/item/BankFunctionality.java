package net.natte.bankstorage.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;

public abstract class BankFunctionality extends Item {

    public BankFunctionality(Settings settings) {
        super(settings);
    }

    @Override
    public boolean canBeNested() {
        return false;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack bank = player.getStackInHand(hand);
        if (bank.getCount() != 1)
            return TypedActionResult.pass(bank);

        if (world.isClient)
            return player.isSneaking() ? TypedActionResult.success(bank) : TypedActionResult.fail(bank);

        if (player.isSneaking()) {
            if (Util.changeBuildMode(bank))
                player.sendMessage(Text.translatable("popup.bankstorage.buildmode."
                        + Util.getOptions(bank).buildMode.toString().toLowerCase()), true);

            return TypedActionResult.success(bank);
        }
        BankItemStorage bankItemStorage = Util.getBankItemStorage(bank, world);
        if (!(player.currentScreenHandler instanceof BankScreenHandler))
            player.openHandledScreen(bankItemStorage);
        // return TypedActionResult.consume(bank);
        return TypedActionResult.success(bank);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        ItemStack bank = player.getStackInHand(context.getHand());

        if (bank.getCount() != 1)
            return ActionResult.PASS;

        BankOptions options = Util.getOrCreateOptions(bank);

        if (options.buildMode == BuildMode.NORMAL || options.buildMode == BuildMode.RANDOM) {
            ItemStack itemStack;
            if (world.isClient) {
                CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(bank);
                itemStack = cachedBankStorage.chooseItemToPlace(options);
            } else {
                BankItemStorage bankItemStorage = Util.getBankItemStorage(bank, world);
                itemStack = bankItemStorage.chooseItemToPlace(options);
            }
            player.setStackInHand(context.getHand(), itemStack);
            ActionResult useResult = itemStack
                    .useOnBlock(new ItemUsageContext(world, player, context.getHand(), itemStack, context.hit));
            player.setStackInHand(context.getHand(), bank);
            if (world.isClient)
                CachedBankStorage.requestCacheUpdate(Util.getUUID(bank));

            return useResult;
        } else {
            return ActionResult.PASS;
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (selected) {

        }

    }

    @Override
    public boolean damage(DamageSource source) {
        // can't take any damage
        return false;
    }
}
