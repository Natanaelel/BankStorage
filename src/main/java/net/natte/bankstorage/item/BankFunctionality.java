package net.natte.bankstorage.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
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
import net.natte.bankstorage.options.PickupMode;
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

    // on .use or .useOnBlock. never returns PASS
    private InteractionResult useBank(Player player, ItemStack bank, InteractionHand hand, boolean usedOnBlock,
                                      @Nullable BlockHitResult hitResult) {

        Level world = player.level();
        boolean isBuildMode = Util.getOrCreateOptions(bank).buildMode().isActive();
        boolean hasBoundKey = Util.isBuildModeToggleKeyBound(player);

        if (bank.getCount() != 1)
            return InteractionResult.FAIL;

        boolean shouldToggleBuildMode = !usedOnBlock && player.isShiftKeyDown() && !hasBoundKey;

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
        BankOptions options = Util.getOrCreateOptions(bankItem);
        BuildMode newBuildMode = Util.isBuildModeCycleKeyBound(player) ? options.buildMode().toggle() : options.buildMode().next();
        bankItem.set(BankStorage.OptionsComponentType, options.withBuildMode(newBuildMode));

        player.displayClientMessage(Component.translatable("popup.bankstorage.buildmode."
                + newBuildMode.toString().toLowerCase()), true);
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

        // prevent blocks which calls setItemInHand in useOn to replace the bank
        ItemStack itemInHand = player.getItemInHand(context.getHand());
        if (itemInHand != bank) {
            if (!world.isClientSide && !player.hasInfiniteMaterials()) {
                ItemStack remaining = bankItemStorage.getItemHandler(PickupMode.ALL).insertItem(itemInHand.copy());
                if (!remaining.isEmpty())
                    player.getInventory().placeItemBackInInventory(remaining);
            }
            player.setItemInHand(context.getHand(), bank);
        }

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

    /**
     * Inserts an item into the bank. On the client this is a no-op (returns the
     * input stack unchanged). On the server, inserts into the BankItemStorage
     * using the ALL pickup mode.
     *
     * @param bankStack the bank item stack
     * @param toInsert  the item stack to insert
     * @param simulate  if true, only simulate the insertion
     * @return the remainder that could not be inserted, or the original stack on client
     */
    private ItemStack stash(ItemStack bankStack, ItemStack toInsert, Player player, boolean simulate) {
        if (bankStack.getCount() != 1 || toInsert.isEmpty() || Util.isDisallowedInBank(toInsert)) {
            return toInsert;
        }

        if (player.level().isClientSide) {
            return toInsert;
        }

        BankItemStorage bankItemStorage = Util.getBankItemStorage(bankStack);
        if (bankItemStorage == null) {
            return toInsert;
        }

        return bankItemStorage.getItemHandler(PickupMode.ALL).insertItem(0, toInsert.copy(), simulate);
    }

    private ItemStack getClientStashRemainder(ItemStack bankStack, ItemStack toInsert) {
        if (bankStack.getCount() != 1 || toInsert.isEmpty() || Util.isDisallowedInBank(toInsert)) {
            return toInsert;
        }

        CachedBankStorage cachedBankStorage = CachedBankStorage.getAndThrottleUpdate(bankStack, 20);
        if (cachedBankStorage == null) {
            return toInsert;
        }

        return cachedBankStorage.getStashRemainder(toInsert, Util.getType(bankStack).stackLimit);
    }

    /**
     * Called when the player right-clicks on a bank slot while holding an item on the cursor.
     * Inserts the cursor item into the bank.
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack bankStack, ItemStack cursorStack,
                                            Slot slot, ClickAction action, Player player,
                                            SlotAccess carriedAccess) {
        if (hasCreativeScreenContainerOpen(player) || bankStack.getCount() != 1
                || action != ClickAction.SECONDARY || !slot.mayPlace(bankStack)) {
            return super.overrideOtherStackedOnMe(bankStack, cursorStack, slot, action, player, carriedAccess);
        }

        ItemStack result = player.level().isClientSide
                ? getClientStashRemainder(bankStack, cursorStack)
                : stash(bankStack, cursorStack, player, false);
        if (result.getCount() != cursorStack.getCount()) {
            carriedAccess.set(result);
            slot.set(bankStack);
            return true;
        }

        return super.overrideOtherStackedOnMe(bankStack, cursorStack, slot, action, player, carriedAccess);
    }

    /**
     * Called when the player has a bank on the cursor and right-clicks on another slot.
     * Takes items from the clicked slot and inserts them into the bank.
     */
    @Override
    public boolean overrideStackedOnOther(ItemStack bankStack, Slot slot, ClickAction action, Player player) {
        if (hasCreativeScreenContainerOpen(player) || bankStack.getCount() != 1
                || action != ClickAction.SECONDARY || !slot.mayPickup(player)) {
            return super.overrideStackedOnOther(bankStack, slot, action, player);
        }

        ItemStack stackToStash = slot.getItem();
        if (stackToStash.isEmpty()) {
            return super.overrideStackedOnOther(bankStack, slot, action, player);
        }

        ItemStack stashResult = player.level().isClientSide
                ? getClientStashRemainder(bankStack, stackToStash)
                : stash(bankStack, stackToStash, player, true);
        if (stashResult.getCount() < stackToStash.getCount()) {
            int countToTake = stackToStash.getCount() - stashResult.getCount();
            while (countToTake > 0) {
                ItemStack takeResult = slot.safeTake(countToTake, countToTake, player);
                if (takeResult.isEmpty()) {
                    break;
                }
                if (!player.level().isClientSide) {
                    stash(bankStack, takeResult, player, false);
                }
                countToTake -= takeResult.getCount();
            }
            return true;
        }

        return super.overrideStackedOnOther(bankStack, slot, action, player);
    }

    private boolean hasCreativeScreenContainerOpen(Player player) {
        return player.level().isClientSide()
                && player.containerMenu.getClass().getName()
                .equals("net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$ItemPickerMenu");
    }
}
