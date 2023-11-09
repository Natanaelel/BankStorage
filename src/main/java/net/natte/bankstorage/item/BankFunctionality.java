package net.natte.bankstorage.item;

import java.util.Random;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.natte.bankstorage.access.SyncedRandomAccess;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.ServerEvents;
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

        boolean isBuildMode = Util.getOrCreateOptions(bank).buildMode != BuildMode.NONE;

        if (world.isClient)
            return player.isSneaking() && (isBuildMode ? Util.isBuildModeKeyUnBound : true)
                    ? TypedActionResult.success(bank)
                    : TypedActionResult.pass(bank);

        if (player.isSneaking() && Util.isBuildModeKeyUnBound) {
            if (Util.isBuildModeKeyUnBound)
                ServerEvents.onToggleBuildMode(((ServerPlayerEntity) player));
            return TypedActionResult.success(bank);
        }
        BankItemStorage bankItemStorage = Util.getBankItemStorage(bank, world);
        if (bankItemStorage == null) {
            player.sendMessage(Text.translatable("popup.bankstorage.unlinked"), true);
            return TypedActionResult.fail(bank);
        }
        if (!(player.currentScreenHandler instanceof BankScreenHandler)
                && (isBuildMode ? Util.isBuildModeKeyUnBound : true))
            player.openHandledScreen(bankItemStorage.withItem(bank));
        // return TypedActionResult.consume(bank);
        return TypedActionResult.success(bank);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        ItemStack bank = player.getStackInHand(context.getHand());

        Random random = ((SyncedRandomAccess) player).bankstorage$getSyncedRandom();

        if (bank.getCount() != 1)
            return ActionResult.PASS;

        BankOptions options = Util.getOrCreateOptions(bank);

        if (options.buildMode == BuildMode.NORMAL || options.buildMode == BuildMode.RANDOM) {
            ItemStack itemStack;
            if (world.isClient) {
                CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(bank);
                if (cachedBankStorage == null) {
                    if (Util.isLink(bank))
                        player.sendMessage(Text.translatable("popup.bankstorage.unlinked"), true);
                    return ActionResult.FAIL;
                }
                itemStack = cachedBankStorage.chooseItemToPlace(options, random);
            } else {
                BankItemStorage bankItemStorage = Util.getBankItemStorage(bank, world);
                if (bankItemStorage == null) {
                    if (Util.isLink(bank))
                        player.sendMessage(Text.translatable("popup.bankstorage.unlinked"), true);
                    return ActionResult.FAIL;
                }
                itemStack = bankItemStorage.chooseItemToPlace(options, random);
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
    public boolean damage(DamageSource source) {
        // can't take any damage
        return false;
    }
}
