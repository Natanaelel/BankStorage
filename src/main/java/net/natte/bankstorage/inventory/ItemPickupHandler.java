package net.natte.bankstorage.inventory;

import com.google.common.collect.Iterables;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.network.ItemStackBobbingAnimationS2C;
import net.natte.bankstorage.options.PickupMode;
import net.natte.bankstorage.util.Util;

public class ItemPickupHandler {
    public static boolean onItemPickup(ItemStack pickedUpStack, PlayerInventory playerInventory) {
        return pickUpStack(pickedUpStack, playerInventory);
    }

    public static boolean pickUpStack(ItemStack pickedUpStack, PlayerInventory playerInventory) {
        System.out.println("hope I am on server");
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
                playerInventory.offHand,
                playerInventory.armor);
        for (ItemStack itemStack : items) {
            ++index;
            if (itemStack.getItem() instanceof BankItem) {
                // int countBefore = itemStack.getCount();
                boolean bankPickedUpAny = false;
                if (!itemStack.hasNbt())
                    continue;
                if (!itemStack.getNbt().contains(BankItem.UUID_KEY))
                    continue;

                BankItemStorage bankItemStorage = BankItem.getBankItemStorage(itemStack, world);
                PickupMode mode = bankItemStorage.options.pickupMode;
                
                if (mode == PickupMode.NONE) {
                    continue;
                }

                if (mode == PickupMode.ALL) {
                    if (addToExistingSlot(bankItemStorage, pickedUpStack))
                        bankPickedUpAny = true;
                    if (addToAnySlot(bankItemStorage, pickedUpStack))
                        bankPickedUpAny = true;
                }

                if (mode == PickupMode.FILTERED || mode == PickupMode.VOID) {

                    if (hasSlotWithItem(bankItemStorage, pickedUpStack)) {
                        if (addToExistingSlot(bankItemStorage, pickedUpStack))
                            bankPickedUpAny = true;
                        if (addToAnySlot(bankItemStorage, pickedUpStack))
                            bankPickedUpAny = true;

                        if (mode == PickupMode.VOID) {
                            if(!pickedUpStack.isEmpty()){
                                bankPickedUpAny = true;
                            }
                            pickedUpStack.setCount(0);
                        }
                    }
                }

                if(bankPickedUpAny){
                    ItemStackBobbingAnimationS2C.send((ServerPlayerEntity)playerInventory.player, index);
                }

                if(pickedUpStack.isEmpty()){
                    return bankPickedUpAny;
                }

            }
        }
        return false;
    }

    public static boolean addToExistingSlot(BankItemStorage bankItemStorage, ItemStack pickedUpStack) {
        if (pickedUpStack.isEmpty())
            return false;

        boolean pickedUpAny = false;
        int slotSize = pickedUpStack.getMaxCount() * bankItemStorage.type.slotStorageMultiplier;

        for (int slot = 0; slot < bankItemStorage.size(); ++slot) {

            ItemStack stackInSlot = bankItemStorage.getStack(slot);

            if (ItemStack.canCombine(stackInSlot, pickedUpStack)) {

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
        int slotSize = pickedUpStack.getMaxCount() * bankItemStorage.type.slotStorageMultiplier;

        for (int slot = 0; slot < bankItemStorage.size(); ++slot) {

            ItemStack stackInSlot = bankItemStorage.getStack(slot);

            if (stackInSlot.isEmpty()) {
                pickedUpAny = true;

                int toMove = Math.min(pickedUpStack.getCount(), slotSize);
                bankItemStorage.setStack(slot, pickedUpStack.copyWithCount(toMove));
                pickedUpStack.decrement(toMove);
                if (pickedUpStack.isEmpty())
                    return pickedUpAny;

            } else if (ItemStack.canCombine(stackInSlot, pickedUpStack)) {

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
            if (ItemStack.canCombine(bankItemStorage.getStack(i), itemStack)) {
                return true;
            }
        }
        return false;
    }

    public void animateIfInserted(boolean didInsert) {
        if (didInsert) {

        }
    }

}
