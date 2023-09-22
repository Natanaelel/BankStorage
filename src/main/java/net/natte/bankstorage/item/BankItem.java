package net.natte.bankstorage.item;

import java.util.List;
import java.util.UUID;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.util.Util;
import net.natte.bankstorage.world.BankStateSaverAndLoader;

public class BankItem extends Item {

    public static final String UUID_KEY = "bank:uuid";

    private BankType type;

    public BankItem(Settings settings, BankType type) {
        super(settings);
        this.type = type;
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
            return TypedActionResult.pass(bank);

        if (player.isSneaking()) {
            BankStorage.onChangeBuildMode((ServerPlayerEntity) player);
            return TypedActionResult.success(bank);
        }

        BankItemStorage bankItemStorage = BankItem.getBankItemStorage(bank, world);
        BuildMode buildMode = bankItemStorage.options.buildMode;
        if (buildMode == BuildMode.NONE) {
            player.openHandledScreen(bankItemStorage);
            return TypedActionResult.success(bank);
        }

        return TypedActionResult.pass(bank);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        ItemStack bank = player.getStackInHand(player.getActiveHand());

        if (bank.getCount() != 1)
            return ActionResult.PASS;

        BankOptions options;
        if (world.isClient) {
            CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(bank);
            options = cachedBankStorage == null ? new BankOptions() : cachedBankStorage.options;
        } else {
            BankItemStorage bankItemStorage = BankItem.getBankItemStorage(bank, world);
            options = bankItemStorage.options;
        }

        if (options.buildMode == BuildMode.NORMAL || options.buildMode == BuildMode.RANDOM) {
            ItemStack itemStack;
            if (world.isClient) {
                CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(bank);
                itemStack = cachedBankStorage.chooseItemToPlace();
            } else {
                BankItemStorage bankItemStorage = BankItem.getBankItemStorage(bank, world);
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
            return ActionResult.FAIL;
        }
    }

    public BankType getType() {
        return this.type;
    }

    public static BankItemStorage getBankItemStorage(ItemStack bank, World world) {

        NbtCompound nbt = bank.getOrCreateNbt();
        UUID uuid;
        if (nbt.contains(UUID_KEY)) {
            uuid = nbt.getUuid(UUID_KEY);
        } else {
            uuid = UUID.randomUUID();
            nbt.putUuid(UUID_KEY, uuid);
        }

        BankType type = ((BankItem) bank.getItem()).getType();
        BankStateSaverAndLoader serverState = BankStateSaverAndLoader.getServerState(world.getServer());
        BankItemStorage bankItemStorage = serverState.getOrCreate(uuid, type, bank.getName());
        // bankItemStorage.options = BankOptions.fromNbt(nbt);
        return bankItemStorage;

    }

    public static BankItemStorage getBankItemStorage(UUID uuid, World world) {

        BankStateSaverAndLoader serverState = BankStateSaverAndLoader.getServerState(world.getServer());
        BankItemStorage bankItemStorage = serverState.get(uuid);

        return bankItemStorage;

    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {

        if (context.isAdvanced())
            if (stack.hasNbt() && stack.getNbt().contains(UUID_KEY))
                tooltip.add(Text.literal(stack.getNbt().getUuid(UUID_KEY).toString()).formatted(Formatting.GRAY));

        super.appendTooltip(stack, world, tooltip, context);
    }

    public static BankOptions getOrCreateOptions(ItemStack itemStack) {
        return BankOptions.fromNbt(itemStack.getOrCreateNbt());
    }

}
