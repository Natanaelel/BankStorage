package net.natte.bankstorage.inventory;

import com.google.common.collect.Iterables;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.PickupMode;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.packet.client.ItemStackBobbingAnimationPacketS2C;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.PacketDistributor;

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

                PickupMode mode = itemStack.getOrDefault(BankStorage.OptionsComponentType, BankOptions.DEFAULT).pickupMode();

                ItemStack notPickedUp = bankItemStorage.getItemHandler(mode).insertItem(pickedUpStack);

                if (notPickedUp.getCount() != pickedUpStack.getCount())
                    bankPickedUpAny = true;

                pickedUpStack.setCount(notPickedUp.getCount());

                if (bankPickedUpAny) {
                    PacketDistributor.sendToPlayer((ServerPlayer) playerInventory.player, new ItemStackBobbingAnimationPacketS2C(index));

                    NetworkUtil.syncCachedBankS2C(bankItemStorage.uuid(), ((ServerPlayer) playerInventory.player));
                }

                if (pickedUpStack.isEmpty()) {
                    return true;
                }

            }
        }
        return false;
    }
}
