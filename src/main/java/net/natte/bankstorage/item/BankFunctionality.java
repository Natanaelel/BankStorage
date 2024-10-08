package net.natte.bankstorage.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.item.tooltip.BankTooltipData;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.screen.BankScreenHandlerFactory;
import net.natte.bankstorage.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class BankFunctionality extends Item {

    public BankFunctionality(Item.Properties settings) {
        super(settings);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }


//    @Override
//    public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack,
//            ItemStack newStack) {
//        return false;
//    }

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
    private InteractionResult useBank(Player player, ItemStack bank, InteractionHand hand, boolean usedOnBlock,
                                      @Nullable BlockHitResult hitResult) {

        Level world = player.level();
        boolean isBuildMode = Util.getOrCreateOptions(bank).buildMode() != BuildMode.NONE;
        boolean hasBoundKey = !Util.isBuildModeKeyUnBound;

        if (bank.getCount() != 1)
            return InteractionResult.FAIL;

        boolean shouldToggleBuildMode = !usedOnBlock && player.isShiftKeyDown() && Util.isBuildModeKeyUnBound;

        if (shouldToggleBuildMode) { // animate
            if (!world.isClientSide)
                toggleBuildMode(bank, (ServerPlayer) player);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        boolean tryOpenWhenUsedOnAir = !usedOnBlock;

        if (tryOpenWhenUsedOnAir) {
            if (isBuildMode && hasBoundKey)
                return InteractionResult.FAIL;
            return tryOpenBank(world, player, hand, bank);
        }

        boolean openWhenUsedOnBlock = usedOnBlock && !isBuildMode;

        if (openWhenUsedOnBlock) {
            return tryOpenBank(world, player, hand, bank);
        }

        return build(new UseOnContext(world, player, hand, bank, hitResult));
    }

    private void toggleBuildMode(ItemStack bankItem, ServerPlayer player) {
        bankItem.update(BankStorage.OptionsComponentType, BankOptions.DEFAULT, BankOptions::nextBuildMode);

        player.displayClientMessage(Component.translatable("popup.bankstorage.buildmode."
                + bankItem.get(BankStorage.OptionsComponentType).buildMode().toString().toLowerCase()), true);
    }

    private InteractionResult tryOpenBank(Level world, Player player, InteractionHand hand, ItemStack bank) {

        if (world.isClientSide)
            return InteractionResult.CONSUME;

        @Nullable
        BankItemStorage bankItemStorage = Util.getBankItemStorage(bank);

        // fail
        if (bankItemStorage == null) {
            player.displayClientMessage(Component.translatable("popup.bankstorage.unlinked"), true);
            return InteractionResult.FAIL;
        }

        // success
        bankItemStorage.usedByPlayerUUID = player.getUUID();
        bankItemStorage.usedByPlayerName = player.getName().getString();

        int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
        BankScreenHandlerFactory screenHandlerFactory = new BankScreenHandlerFactory(bankItemStorage.type(), bankItemStorage, bank, slot, ContainerLevelAccess.NULL);

        player.openMenu(screenHandlerFactory, screenHandlerFactory::writeScreenOpeningData);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult build(UseOnContext context) {

        Player player = context.getPlayer();
        ItemStack bank = context.getItemInHand();
        Level world = context.getLevel();

        Random random = world.isClientSide ? Util.clientSyncedRandom : player.getData(BankStorage.SYNCED_RANDOM_ATTACHMENT);

        BankOptions options = Util.getOrCreateOptions(bank);
        int selectedSlot = bank.getOrDefault(BankStorage.SelectedSlotComponentType, 0);

        BankItemStorage bankItemStorage = null;
        ItemStack blockToPlace;
        if (world.isClientSide) {
            @Nullable
            CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(bank);
            if (cachedBankStorage == null) {
                if (Util.isLink(bank))
                    player.displayClientMessage(Component.translatable("popup.bankstorage.unlinked"), true);
                return InteractionResult.FAIL;
            }
            blockToPlace = cachedBankStorage.chooseItemToPlace(options, random, selectedSlot);
        } else {
            bankItemStorage = Util.getBankItemStorage(bank);
            if (bankItemStorage == null) {
                if (Util.isLink(bank))
                    player.displayClientMessage(Component.translatable("popup.bankstorage.unlinked"), true);
                return InteractionResult.FAIL;
            }
            bankItemStorage.usedByPlayerUUID = player.getUUID();
            bankItemStorage.usedByPlayerName = player.getName().getString();

            blockToPlace = bankItemStorage.chooseItemToPlace(options, random, selectedSlot);
        }

        // prevent ae2wtlib restock dupe by placing from stack with count 1
        // https://github.com/Mari023/AE2WirelessTerminalLibrary/blob/9a971887fcc7dced398297a2c6cb9057633b9883/src/main/java/de/mari_023/ae2wtlib/AE2wtlibEvents.java#L35
        int count = blockToPlace.getCount();
        blockToPlace.setCount(1);
        InteractionResult useResult = blockToPlace
                .useOn(new UseOnContext(world, player, context.getHand(), blockToPlace, context.hitResult));

        blockToPlace.setCount(blockToPlace.getCount() == 1 ? count : count - 1);

        if (world.isClientSide)
            CachedBankStorage.requestCacheUpdate(Util.getUUID(bank));

        if (bankItemStorage != null) {
            bankItemStorage.markDirty();
        }

        return useResult;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {

        ItemStack bank = player.getItemInHand(hand);
        InteractionResult result = useBank(player, bank, hand, false, null);

        return switch (result) {
            case CONSUME, CONSUME_PARTIAL, FAIL, SUCCESS -> InteractionResultHolder.consume(bank);
            case PASS -> InteractionResultHolder.pass(bank);
            case SUCCESS_NO_ITEM_USED -> InteractionResultHolder.success(bank);
        };
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return useBank(context.getPlayer(), context.getItemInHand(), context.getHand(), true, context.hitResult);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {

        if (Util.isShiftDown.get())
            return Optional.empty();

        CachedBankStorage cachedBankStorage = CachedBankStorage.getAndThrottleUpdate(stack, 20);

        if (cachedBankStorage == null)
            return Optional.empty();

        List<ItemStack> nonEmptyItems = cachedBankStorage.getNonEmptyItems();

        if (nonEmptyItems.isEmpty())
            return Optional.empty();

        return Optional.of(new BankTooltipData(nonEmptyItems));
    }

    @Override
    public boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
        return true;
    }
}
