package net.natte.bankstorage.inventory;

import java.util.List;

import com.google.common.collect.Iterables;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.PickupMode;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.packet.client.ItemStackBobbingAnimationPacketS2C;
import net.natte.bankstorage.util.Util;

public class ItemPickupHandler {

    public static boolean onItemPickup(ItemStack pickedUpStack, PlayerInventory playerInventory) {
        return pickUpStack(pickedUpStack, playerInventory);
    }

    public static boolean pickUpStack(ItemStack pickedUpStack, PlayerInventory playerInventory) {
        World world = playerInventory.player.getWorld();

        if (world.isClient)
            return false;
        if (pickedUpStack.isEmpty())
            return false;
        if (!Util.isAllowedInBank(pickedUpStack))
            return false;

        int index = -1;
        Iterable<ItemStack> items = Iterables.concat(
                playerInventory.main,
                playerInventory.armor,
                playerInventory.offHand);

        for (ItemStack itemStack : items) {
            ++index;
            if (Util.isBankLike(itemStack)) {
                boolean bankPickedUpAny = false;
                if (!itemStack.contains(BankStorage.UUIDComponentType))
                    continue;

                BankItemStorage bankItemStorage = Util.getBankItemStorage(itemStack, world);
                // PickupMode mode = bankItemStorage.options.pickupMode;
                PickupMode mode = Util.getOrCreateOptions(itemStack).pickupMode;

                if (mode == PickupMode.NONE) {
                    continue;
                }

                if (mode == PickupMode.ALL) {
                    if (addToLockedSlot(bankItemStorage, pickedUpStack))
                        bankPickedUpAny = true;
                    if (addToExistingSlot(bankItemStorage, pickedUpStack))
                        bankPickedUpAny = true;
                    if (addToAnySlot(bankItemStorage, pickedUpStack))
                        bankPickedUpAny = true;
                }

                if (mode == PickupMode.FILTERED || mode == PickupMode.VOID) {

                    if (hasSlotWithItem(bankItemStorage, pickedUpStack)) {
                        if (addToLockedSlot(bankItemStorage, pickedUpStack))
                            bankPickedUpAny = true;
                        if (addToExistingSlot(bankItemStorage, pickedUpStack))
                            bankPickedUpAny = true;
                        if (mode == PickupMode.FILTERED)
                            if (addToAnySlot(bankItemStorage, pickedUpStack))
                                bankPickedUpAny = true;

                        if (mode == PickupMode.VOID) {
                            if (!pickedUpStack.isEmpty()) {
                                bankPickedUpAny = true;
                            }
                            pickedUpStack.setCount(0);
                        }
                    }
                }

                if (bankPickedUpAny) {
                    ServerPlayNetworking.send(((ServerPlayerEntity) playerInventory.player),
                            new ItemStackBobbingAnimationPacketS2C(index));

                    // only update client cache if needed; when the bank is selected and in
                    // buildmode
                    if (playerInventory.player.getInventory().selectedSlot == index)
                        NetworkUtil.syncCachedBankIfBuildModeS2C(bankItemStorage.uuid,
                                (ServerPlayerEntity) playerInventory.player, itemStack);
                }

                if (pickedUpStack.isEmpty()) {
                    return bankPickedUpAny;
                }

            }
        }
        return false;
    }

    public static boolean addToLockedSlot(BankItemStorage bankItemStorage, ItemStack pickedUpStack) {
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
            if (ItemStack.areItemsAndComponentsEqual(bankItemStorage.getLockedStack(index), pickedUpStack)) {
                ItemStack stackInSlot = bankItemStorage.getStack(index);
                int spaceLeft = slotSize - stackInSlot.getCount();
                int toMove = Math.min(pickedUpStack.getCount(), spaceLeft);
                if (toMove > 0) {
                    pickedUpAny = true;
                    if (stackInSlot.isEmpty()) {
                        bankItemStorage.setStack(index, pickedUpStack.copyWithCount(toMove));
                    } else {
                        stackInSlot.increment(toMove);
                    }
                    pickedUpStack.decrement(toMove);
                    bankItemStorage.markDirty();

                    if (pickedUpStack.isEmpty())
                        return pickedUpAny;
                }
            }
        }
        ;

        return pickedUpAny;
    }

    public static boolean addToExistingSlot(BankItemStorage bankItemStorage, ItemStack pickedUpStack) {
        if (pickedUpStack.isEmpty())
            return false;

        boolean pickedUpAny = false;
        // int slotSize = pickedUpStack.getMaxCount() *
        // bankItemStorage.type.slotStorageMultiplier;
        int slotSize = bankItemStorage.type.stackLimit;

        for (int slot = 0; slot < bankItemStorage.size(); ++slot) {

            ItemStack stackInSlot = bankItemStorage.getStack(slot);

            if (ItemStack.areItemsAndComponentsEqual(stackInSlot, pickedUpStack)) {

                int spaceLeft = slotSize - stackInSlot.getCount();
                int toMove = Math.min(pickedUpStack.getCount(), spaceLeft);

                if (toMove > 0) {
                    pickedUpAny = true;

                    stackInSlot.increment(toMove);
                    pickedUpStack.decrement(toMove);
                    bankItemStorage.markDirty();

                    if (pickedUpStack.isEmpty())
                        return pickedUpAny;
                }
            }
        }
        return pickedUpAny;
    }

    public static boolean addToAnySlot(BankItemStorage bankItemStorage, ItemStack pickedUpStack) {
        if (pickedUpStack.isEmpty())
            return false;

        boolean pickedUpAny = false;
        // int slotSize = pickedUpStack.getMaxCount() *
        // bankItemStorage.type.slotStorageMultiplier;
        int slotSize = bankItemStorage.type.stackLimit;

        for (int slot = 0; slot < bankItemStorage.size(); ++slot) {

            ItemStack stackInSlot = bankItemStorage.getStack(slot);
            ItemStack lockedStack = bankItemStorage.getLockedStack(slot);

            if (lockedStack != null && !ItemStack.areItemsAndComponentsEqual(pickedUpStack, lockedStack))
                continue;

            if (stackInSlot.isEmpty()) {
                pickedUpAny = true;

                int toMove = Math.min(pickedUpStack.getCount(), slotSize);
                bankItemStorage.setStack(slot, pickedUpStack.copyWithCount(toMove));
                pickedUpStack.decrement(toMove);
                if (pickedUpStack.isEmpty())
                    return pickedUpAny;

            } else if (ItemStack.areItemsAndComponentsEqual(stackInSlot, pickedUpStack)) {

                int spaceLeft = slotSize - stackInSlot.getCount();
                int toMove = Math.min(pickedUpStack.getCount(), spaceLeft);

                if (toMove > 0) {
                    pickedUpAny = true;

                    stackInSlot.increment(toMove);
                    pickedUpStack.decrement(toMove);
                    bankItemStorage.markDirty();
                    if (pickedUpStack.isEmpty())
                        return pickedUpAny;
                }
            }
        }
        return pickedUpAny;
    }

    public static boolean hasSlotWithItem(BankItemStorage bankItemStorage, ItemStack itemStack) {
        for (int i = 0; i < bankItemStorage.size(); ++i) {
            if (ItemStack.areItemsAndComponentsEqual(bankItemStorage.getStack(i), itemStack)) {
                return true;
            }
        }
        for (int i : bankItemStorage.getlockedSlots().keySet()) {
            if (ItemStack.areItemsAndComponentsEqual(bankItemStorage.getLockedStack(i), itemStack)) {
                return true;
            }
        }
        return false;
    }
}
