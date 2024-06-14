package net.natte.bankstorage.inventory;

import java.util.List;

import com.google.common.collect.Iterables;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.PickupMode;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.packet.client.ItemStackBobbingAnimationPacketS2C;
import net.natte.bankstorage.util.Util;

public class ItemPickupHandler {

    // returns true if the whole stack got picked up. modifies the itemstack
    public static boolean pickUpStack(ItemStack pickedUpStack, Inventory playerInventory) {
        Level world = playerInventory.player.level();

        if (world.isClientSide)
            return false;
        if (pickedUpStack.isEmpty())
            return false;
        if (!Util.isAllowedInBank(pickedUpStack))
            return false;

        int index = -1;
        Iterable<ItemStack> items = Iterables.concat(
                playerInventory.items,
                playerInventory.armor,
                playerInventory.offhand);

        for (ItemStack itemStack : items) {
            ++index;
            if (Util.isBankLike(itemStack)) {
                boolean bankPickedUpAny = false;
                if (!itemStack.has(BankStorage.UUIDComponentType))
                    continue;

                BankItemStorage bankItemStorage = Util.getBankItemStorage(itemStack);
                if (bankItemStorage == null)
                    continue;

                PickupMode mode = Util.getOrCreateOptions(itemStack).pickupMode;

                ItemStack notPickedUp = bankItemStorage.getItemHandler(mode).insertItem(pickedUpStack);

                if (notPickedUp.getCount() != pickedUpStack.getCount())
                    bankPickedUpAny = true;

                pickedUpStack.setCount(notPickedUp.getCount());

//                if (mode == PickupMode.NONE) {
//                    continue;
//                }
//
//                if (mode == PickupMode.ALL) {
//                    if (addToLockedSlot(bankItemStorage, pickedUpStack))
//                        bankPickedUpAny = true;
//                    if (addToExistingSlot(bankItemStorage, pickedUpStack))
//                        bankPickedUpAny = true;
//                    if (addToAnySlot(bankItemStorage, pickedUpStack))
//                        bankPickedUpAny = true;
//                }
//
//                if (mode == PickupMode.FILTERED || mode == PickupMode.VOID) {
//
//                    if (hasSlotWithItem(bankItemStorage, pickedUpStack)) {
//                        if (addToLockedSlot(bankItemStorage, pickedUpStack))
//                            bankPickedUpAny = true;
//                        if (addToExistingSlot(bankItemStorage, pickedUpStack))
//                            bankPickedUpAny = true;
//                        if (mode == PickupMode.FILTERED)
//                            if (addToAnySlot(bankItemStorage, pickedUpStack))
//                                bankPickedUpAny = true;
//
//                        if (mode == PickupMode.VOID) {
//                            if (!pickedUpStack.isEmpty()) {
//                                bankPickedUpAny = true;
//                            }
//                            pickedUpStack.setCount(0);
//                        }
//                    }
//                }

                if (bankPickedUpAny) {
                    ((ServerPlayer) playerInventory.player).connection.send(new ItemStackBobbingAnimationPacketS2C(index));

                    NetworkUtil.syncCachedBankS2C(bankItemStorage.uuid, ((ServerPlayer) playerInventory.player));
                    // only update client cache if needed; when the bank is selected and in
                    // buildmode
//                    if (playerInventory.player.getInventory().selected == index)
//                        NetworkUtil.syncCachedBankIfBuildModeS2C(bankItemStorage.uuid,
//                                (ServerPlayerEntity) playerInventory.player, itemStack);
                }

                if (pickedUpStack.isEmpty()) {
                    return true;
                }

            }
        }
        return false;
    }
    /*
    private static boolean addToLockedSlot(BankItemStorage bankItemStorage, ItemStack pickedUpStack) {
        if (pickedUpStack.isEmpty())
            return false;

        boolean pickedUpAny = false;

        int slotSize = bankItemStorage.type.stackLimit;
        List<Integer> sortedKeys = bankItemStorage
                .getlockedSlots()
                .keySet()
                .stream()
                .sorted()
                .toList();
        for (int index : sortedKeys) {
            if (ItemStack.isSameItemSameComponents(bankItemStorage.getLockedStack(index), pickedUpStack)) {
                ItemStack stackInSlot = bankItemStorage.getItem(index);
                int spaceLeft = slotSize - stackInSlot.getCount();
                int toMove = Math.min(pickedUpStack.getCount(), spaceLeft);
                if (toMove > 0) {
                    pickedUpAny = true;
                    if (stackInSlot.isEmpty()) {
                        bankItemStorage.setItem(index, pickedUpStack.copyWithCount(toMove));
                    } else {
                        stackInSlot.grow(toMove);
                    }
                    pickedUpStack.shrink(toMove);
                    bankItemStorage.markDirty();

                    if (pickedUpStack.isEmpty())
                        return pickedUpAny;
                }
            }
        }
        ;

        return pickedUpAny;
    }

    private static boolean addToExistingSlot(BankItemStorage bankItemStorage, ItemStack pickedUpStack) {
        if (pickedUpStack.isEmpty())
            return false;

        boolean pickedUpAny = false;
        // int slotSize = pickedUpStack.getMaxCount() *
        // bankItemStorage.type.slotStorageMultiplier;
        int slotSize = bankItemStorage.type.stackLimit;

        for (int slot = 0; slot < bankItemStorage.size(); ++slot) {

            ItemStack stackInSlot = bankItemStorage.getItems().get(slot);

            if (ItemStack.isSameItemSameComponents(stackInSlot, pickedUpStack)) {

                int spaceLeft = slotSize - stackInSlot.getCount();
                int toMove = Math.min(pickedUpStack.getCount(), spaceLeft);

                if (toMove > 0) {
                    pickedUpAny = true;

                    stackInSlot.grow(toMove);
                    pickedUpStack.shrink(toMove);
                    bankItemStorage.markDirty();

                    if (pickedUpStack.isEmpty())
                        return pickedUpAny;
                }
            }
        }
        return pickedUpAny;
    }

    private static boolean addToAnySlot(BankItemStorage bankItemStorage, ItemStack pickedUpStack) {
        if (pickedUpStack.isEmpty())
            return false;

        boolean pickedUpAny = false;
        // int slotSize = pickedUpStack.getMaxCount() *
        // bankItemStorage.type.slotStorageMultiplier;
        int slotSize = bankItemStorage.type.stackLimit;

        for (int slot = 0; slot < bankItemStorage.size(); ++slot) {

            ItemStack stackInSlot = bankItemStorage.getItem(slot);
            ItemStack lockedStack = bankItemStorage.getLockedStack(slot);

            if (lockedStack != null && !ItemStack.isSameItemSameComponents(pickedUpStack, lockedStack))
                continue;

            if (stackInSlot.isEmpty()) {
                pickedUpAny = true;

                int toMove = Math.min(pickedUpStack.getCount(), slotSize);
                bankItemStorage.setItem(slot, pickedUpStack.copyWithCount(toMove));
                pickedUpStack.shrink(toMove);
                if (pickedUpStack.isEmpty())
                    return pickedUpAny;

            } else if (ItemStack.isSameItemSameComponents(stackInSlot, pickedUpStack)) {

                int spaceLeft = slotSize - stackInSlot.getCount();
                int toMove = Math.min(pickedUpStack.getCount(), spaceLeft);

                if (toMove > 0) {
                    pickedUpAny = true;

                    stackInSlot.grow(toMove);
                    pickedUpStack.shrink(toMove);
                    bankItemStorage.markDirty();
                    if (pickedUpStack.isEmpty())
                        return pickedUpAny;
                }
            }
        }
        return pickedUpAny;
    }

    private static boolean hasSlotWithItem(BankItemStorage bankItemStorage, ItemStack itemStack) {
        for (int i = 0; i < bankItemStorage.size(); ++i) {
            if (ItemStack.isSameItemSameComponents(bankItemStorage.getItem(i), itemStack)) {
                return true;
            }
        }
        for (int i : bankItemStorage.getlockedSlots().keySet()) {
            if (ItemStack.isSameItemSameComponents(bankItemStorage.getLockedStack(i), itemStack)) {
                return true;
            }
        }
        return false;
    }
    */
}
