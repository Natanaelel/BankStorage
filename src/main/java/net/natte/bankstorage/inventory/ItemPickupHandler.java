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
    public static void onItemPickup(ItemStack pickedUpStack, PlayerInventory playerInventory) {
        System.out.println("hope I am on server");
        World world = playerInventory.player.getWorld();
        if (world.isClient)
            return;

        if (!Util.isAllowedInBank(pickedUpStack))
            return;

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

                if (mode == PickupMode.NONE){
                    System.out.println("none, continue");
                    continue;
                }

                if (mode == PickupMode.ALL) {
                    System.out.println("all, add");
                    System.out.println("before: " + pickedUpStack);
                    System.out.println(bankItemStorage.addStack(pickedUpStack));
                    System.out.println("after: " + pickedUpStack);
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
                        bankItemStorage.addStack(pickedUpStack);
                        if (mode == PickupMode.VOID) {
                            pickedUpStack.setCount(0);
                        }
                    }
                }

                if (pickedUpStack.isEmpty())
                    return;
                // return;
                // return;
                // }

                // }

                // if(bankItemStorage.canInsert(pickedUpStack)){
                // bankItemStorage.
                // }
            }
        }

    }
}
