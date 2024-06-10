package net.natte.bankstorage.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.neoforged.neoforge.network.IContainerFactory;
import org.jetbrains.annotations.Nullable;

public class BankScreenHandlerFactory implements IContainerFactory<BankScreenHandler> {

    //    private @Nullable BankItemStorage bank;
//    private ItemStack bankItem;
//    // which inventoryslot bank is in, or -1
//    private int slot;
//    private ContainerLevelAccess screenHandlerContext;
    private BankType type;

    public BankScreenHandlerFactory(BankType type) {
        this.type = type;
    }

    @Override
    public BankScreenHandler create(int windowId, Inventory inv, RegistryFriendlyByteBuf data) {
        return createClientScreenHandler(windowId, inv, data);
    }

    // called client side only
    private BankScreenHandler createClientScreenHandler(int syncId, Inventory playerInventory,
                                                        RegistryFriendlyByteBuf buf) {

        ItemStack bankItem = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        int slot = buf.readInt();


        return new BankScreenHandler(syncId, playerInventory,
                new BankItemStorage(type, null), type, ContainerLevelAccess.NULL);
    }

}
