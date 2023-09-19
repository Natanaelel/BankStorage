package net.natte.bankstorage.inventory;

import com.google.common.collect.Iterables;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.options.PickupMode;
import net.natte.bankstorage.util.Util;

public class ItemPickupHandler {
    public static boolean onItemPickup(ItemStack pickedUpStack, PlayerInventory playerInventory) {
        System.out.println("hope I am on server");
        World world = playerInventory.player.getWorld();
        if (world.isClient)
            return false;
        if (pickedUpStack.isEmpty())
            return false;
        if (!Util.isAllowedInBank(pickedUpStack))
            return false;

        Iterable<ItemStack> items = Iterables.concat(
                playerInventory.main,
                playerInventory.offHand,
                playerInventory.armor);
        System.out.println("looping");
        for (ItemStack itemStack : items) {
            if (itemStack.getItem() instanceof BankItem) {
                if (!itemStack.hasNbt())
                    continue;
                if (!itemStack.getNbt().contains(BankItem.UUID_KEY))
                    continue;

                BankItemStorage bankItemStorage = BankItem.getBankItemStorage(itemStack, world);
                PickupMode mode = bankItemStorage.options.pickupMode;
                System.out.println(mode);
                if (mode == PickupMode.NONE) {
                    continue;
                }

                if (mode == PickupMode.ALL) {
                    addToExistingSlot(bankItemStorage, pickedUpStack);
                    if (pickedUpStack.isEmpty()) return true;
                    addToAnySlot(bankItemStorage, pickedUpStack);
                    if (pickedUpStack.isEmpty()) return true;
                }

                if (mode == PickupMode.FILTERED || mode == PickupMode.VOID) {
                    boolean canInsert = false;
                    for (int i = 0; i < bankItemStorage.size(); ++i) {
                        if (ItemStack.canCombine(bankItemStorage.getStack(i), pickedUpStack)) {
                            canInsert = true;
                            break;
                        }
                    }
                    if (canInsert) {
                        addToExistingSlot(bankItemStorage, pickedUpStack);
                        if (pickedUpStack.isEmpty()) return true;
                        addToAnySlot(bankItemStorage, pickedUpStack);
                        if (pickedUpStack.isEmpty()) return true;

                        if (mode == PickupMode.VOID) {
                            pickedUpStack.setCount(0);
                            return true;
                        }
                    }
                }


            }
        }
        return false;
    }

    public static void addToExistingSlot(BankItemStorage bankItemStorage, ItemStack pickedUpStack) {

        int slotSize = pickedUpStack.getMaxCount() * bankItemStorage.type.slotStorageMultiplier;

        for (int slot = 0; slot < bankItemStorage.size(); ++slot) {

            ItemStack stackInSlot = bankItemStorage.getStack(slot);

            if (ItemStack.canCombine(stackInSlot, pickedUpStack)) {

                int spaceLeft = slotSize - stackInSlot.getCount();
                int toMove = Math.min(pickedUpStack.getCount(), spaceLeft);

                if (toMove > 0) {
                    stackInSlot.increment(toMove);
                    pickedUpStack.decrement(toMove);
                    bankItemStorage.markDirty();
                    if (pickedUpStack.isEmpty())
                        return;
                }
            }
        }
    }

    public static void addToAnySlot(BankItemStorage bankItemStorage, ItemStack pickedUpStack) {

        int slotSize = pickedUpStack.getMaxCount() * bankItemStorage.type.slotStorageMultiplier;

        for (int slot = 0; slot < bankItemStorage.size(); ++slot) {

            ItemStack stackInSlot = bankItemStorage.getStack(slot);

            if (stackInSlot.isEmpty()) {
                int toMove = Math.min(pickedUpStack.getCount(), slotSize);
                bankItemStorage.setStack(slot, pickedUpStack.copyWithCount(toMove));
                pickedUpStack.decrement(toMove);
                if (pickedUpStack.isEmpty())
                    return;
            } else if (ItemStack.canCombine(stackInSlot, pickedUpStack)) {

                int spaceLeft = slotSize - stackInSlot.getCount();
                int toMove = Math.min(pickedUpStack.getCount(), spaceLeft);

                if (toMove > 0) {
                    stackInSlot.increment(toMove);
                    pickedUpStack.decrement(toMove);
                    bankItemStorage.markDirty();
                    if (pickedUpStack.isEmpty())
                        return;
                }
            }
        }
    }
}
