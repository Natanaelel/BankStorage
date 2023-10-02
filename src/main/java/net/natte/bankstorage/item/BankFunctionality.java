package net.natte.bankstorage.item;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.util.Util;

public abstract class BankFunctionality extends Item {

    public static final String UUID_KEY = "bank:uuid";

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
        System.out.println(bank);
        if (bank.getCount() != 1)
            return TypedActionResult.pass(bank);

        if (world.isClient)
            return TypedActionResult.pass(bank);

        if (player.isSneaking()) {
            BankStorage.onChangeBuildMode((ServerPlayerEntity) player, bank);
            return TypedActionResult.success(bank);
        }

        BankItemStorage bankItemStorage = Util.getBankItemStorage(bank, world);
        player.openHandledScreen(bankItemStorage);
        return TypedActionResult.success(bank);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        ItemStack bank = player.getStackInHand(context.getHand());

        if (bank.getCount() != 1)
            return ActionResult.PASS;

        BankOptions options;
        if (world.isClient) {
            CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(bank);
            options = cachedBankStorage == null ? new BankOptions() : cachedBankStorage.options;
        } else {
            BankItemStorage bankItemStorage = Util.getBankItemStorage(bank, world);
            options = bankItemStorage.options;
        }

        if (options.buildMode == BuildMode.NORMAL || options.buildMode == BuildMode.RANDOM) {
            ItemStack itemStack;
            if (world.isClient) {
                CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(bank);
                itemStack = cachedBankStorage.chooseItemToPlace();
            } else {
                BankItemStorage bankItemStorage = Util.getBankItemStorage(bank, world);
                itemStack = bankItemStorage.chooseItemToPlace();
            }
            player.setStackInHand(context.getHand(), itemStack);
            ActionResult useResult = itemStack
                    .useOnBlock(new ItemUsageContext(world, player, context.getHand(), itemStack, context.hit));
            player.setStackInHand(context.getHand(), bank);
            if (world.isClient)
                CachedBankStorage.bankRequestQueue.add(Util.getUUID(bank));

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
