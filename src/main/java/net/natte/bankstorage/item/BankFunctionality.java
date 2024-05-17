package net.natte.bankstorage.item;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.natte.bankstorage.access.SyncedRandomAccess;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.item.tooltip.BankTooltipData;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
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
    public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack,
            ItemStack newStack) {
        return false;
    }

    // isBuildMode:B usedOnBlock:B isSneaking:B hasToggleKey:B -> shouldOpen:B
    // 78: 0 0 0 0 -> 1
    // 78: 0 0 0 1 -> 1
    // 70: 0 0 1 0 -> 0 (toggle build mode) animate
    // 78: 0 0 1 1 -> 1
    // 0 1 0 0 -> 1 (unless clicked chest etc)
    // 0 1 0 1 -> 1 (unless clicked chest etc)
    // 0 1 1 0 -> 1 (:force open regardless of target)
    // 0 1 1 1 -> 1 (:force open regardless of target)
    // 78: 1 0 0 0 -> 1 (:open when build mode because no bound key)
    // 78: 1 0 0 1 -> 0 (:^ but has bound key)
    // 70: 1 0 1 0 -> 0 (toggle build mode) animate
    // 78: 1 0 1 1 -> 0 (^ but has bound key)
    // 1 1 0 0 -> 0 (build unless chest) animate
    // 1 1 0 1 -> 0 (build unless chest) animate
    // 1 1 1 0 -> 0 (build) animate
    // 1 1 1 1 -> 0 (build) animate

    // on .use or .useOnBlock. never returns PASS
    private ActionResult useBank(PlayerEntity player, Hand hand, boolean usedOnBlock,
            @Nullable BlockHitResult hitResult) {

        ItemStack bank = player.getStackInHand(hand);
        World world = player.getWorld();
        boolean isBuildMode = Util.getOrCreateOptions(bank).buildMode != BuildMode.NONE;
        boolean hasBoundKey = !Util.isBuildModeKeyUnBound;

        if (bank.getCount() != 1)
            return ActionResult.FAIL;

        boolean shouldToggleBuildMode = !usedOnBlock && player.isSneaking() && Util.isBuildModeKeyUnBound;

        if (shouldToggleBuildMode) { // animate
            if (world.isClient)
                Util.onToggleBuildMode.accept(player);
            return ActionResult.CONSUME;
        }

        boolean tryOpenWhenUsedOnAir = !usedOnBlock;

        if (tryOpenWhenUsedOnAir) {
            if (isBuildMode && hasBoundKey)
                return ActionResult.FAIL;
            return tryOpenBank(world, player, bank);
        }

        boolean openWhenUsedOnBlock = usedOnBlock && !isBuildMode;

        if (openWhenUsedOnBlock) {
            return tryOpenBank(world, player, bank);
        }

        return build(new ItemUsageContext(world, player, hand, bank, hitResult));
    }

    private ActionResult tryOpenBank(World world, PlayerEntity player, ItemStack bank) {

        if (world.isClient)
            return ActionResult.CONSUME;

        @Nullable
        BankItemStorage bankItemStorage = Util.getBankItemStorage(bank, world);

        // fail
        if (bankItemStorage == null) {
            player.sendMessage(Text.translatable("popup.bankstorage.unlinked"), true);
            return ActionResult.FAIL;
        }

        // success
        bankItemStorage.usedByPlayerUUID = player.getUuid();
        bankItemStorage.usedByPlayerName = player.getName().getString();

        player.openHandledScreen(bankItemStorage.withItem(bank));
        return ActionResult.CONSUME;
    }

    private ActionResult build(ItemUsageContext context) {

        PlayerEntity player = context.getPlayer();
        ItemStack bank = context.getStack();
        World world = context.getWorld();

        Random random = world.isClient ? Util.clientSyncedRandom
                : ((SyncedRandomAccess) player).bankstorage$getSyncedRandom();

        BankOptions options = Util.getOrCreateOptions(bank);

        BankItemStorage bankItemStorage = null;
        ItemStack blockToPlace;
        if (world.isClient) {
            @Nullable
            CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(bank);
            if (cachedBankStorage == null) {
                if (Util.isLink(bank))
                    player.sendMessage(Text.translatable("popup.bankstorage.unlinked"), true);
                return ActionResult.FAIL;
            }
            blockToPlace = cachedBankStorage.chooseItemToPlace(options, random);
        } else {
            bankItemStorage = Util.getBankItemStorage(bank, world);
            if (bankItemStorage == null) {
                if (Util.isLink(bank))
                    player.sendMessage(Text.translatable("popup.bankstorage.unlinked"), true);
                return ActionResult.FAIL;
            }
            bankItemStorage.usedByPlayerUUID = player.getUuid();
            bankItemStorage.usedByPlayerName = player.getName().getString();

            blockToPlace = bankItemStorage.chooseItemToPlace(options, random);
        }

        // prevent ae2wtlib restock dupe by placing from stack with count 1
        // https://github.com/Mari023/AE2WirelessTerminalLibrary/blob/9a971887fcc7dced398297a2c6cb9057633b9883/src/main/java/de/mari_023/ae2wtlib/AE2wtlibEvents.java#L35
        int count = blockToPlace.getCount();
        blockToPlace.setCount(1);
        ActionResult useResult = blockToPlace
                .useOnBlock(new ItemUsageContext(world, player, context.getHand(), blockToPlace, context.hit));

        blockToPlace.setCount(blockToPlace.getCount() == 1 ? count : count - 1);

        if (world.isClient)
            CachedBankStorage.requestCacheUpdate(Util.getUUID(bank));

        if (bankItemStorage != null) {
            bankItemStorage.markDirty();
        }

        return useResult;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack bank = player.getStackInHand(hand);
        ActionResult result = useBank(player, hand, false, null);

        return switch (result) {
            case CONSUME -> TypedActionResult.fail(bank);
            case CONSUME_PARTIAL -> TypedActionResult.consume(bank);
            case FAIL -> TypedActionResult.fail(bank);
            case PASS -> TypedActionResult.pass(bank);
            case SUCCESS -> TypedActionResult.success(bank);
            case SUCCESS_NO_ITEM_USED -> TypedActionResult.success(bank);
        };
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return useBank(context.getPlayer(), context.getHand(), true, context.hit);
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {

        if (Util.isShiftDown.get())
            return Optional.empty();

        CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(stack);

        if (cachedBankStorage == null)
            return Optional.empty();

        List<ItemStack> items = cachedBankStorage.nonEmptyItems;

        if (items.isEmpty())
            return Optional.empty();

        return Optional.of(new BankTooltipData(cachedBankStorage.nonEmptyItems));
    }
}
