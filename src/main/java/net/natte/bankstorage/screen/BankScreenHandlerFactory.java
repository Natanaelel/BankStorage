package net.natte.bankstorage.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.neoforged.neoforge.network.IContainerFactory;
import org.jetbrains.annotations.Nullable;

public class BankScreenHandlerFactory implements MenuProvider {

    private BankType type;
    private @Nullable BankItemStorage bank;
    private ItemStack bankItem;
    //    // which inventoryslot bank is in, or -1
    private int slot;
    private ContainerLevelAccess screenHandlerContext;


    public BankScreenHandlerFactory(BankType type, @Nullable BankItemStorage bank, ItemStack bankItem, int slot, ContainerLevelAccess screenHandlerContext) {
        this.type = type;
        this.bank = bank;
        this.bankItem = bankItem;
        this.slot = slot;
        this.screenHandlerContext = screenHandlerContext;
    }

    public void writeScreenOpeningData(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(this.type.getName());
        ItemStack.STREAM_CODEC.encode(buf, this.bankItem);
        buf.writeInt(this.slot);

    }

    // called client side only
    public static BankScreenHandler createClientScreenHandler(int syncId, Inventory playerInventory,
                                                              RegistryFriendlyByteBuf buf) {
        BankType type = BankType.getBankTypeFromName(buf.readUtf());
        ItemStack bankItem = ItemStack.STREAM_CODEC.decode(buf);
        int slot = buf.readInt();


        return new BankScreenHandler(syncId, playerInventory, type, bankItem, slot,
                new BankItemStorage(type, null), ContainerLevelAccess.NULL);
    }

    @Override
    public Component getDisplayName() {
        return bankItem.getDisplayName();
    }

    @Nullable
    @Override
    public BankScreenHandler createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return null;
    }
}
