package net.natte.bankstorage.packet.server;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.screen.BankScreenHandlerFactory;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenBankFromKeyBindPacketC2S() implements CustomPacketPayload {

    public static final OpenBankFromKeyBindPacketC2S INSTANCE = new OpenBankFromKeyBindPacketC2S();

    public static final Type<OpenBankFromKeyBindPacketC2S> TYPE = new Type<>(Util.ID("open_bank_from_keybind_c2s"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenBankFromKeyBindPacketC2S> STREAM_CODEC = StreamCodec.unit(INSTANCE);


    @Override
    public Type<OpenBankFromKeyBindPacketC2S> type() {
        return TYPE;
    }

    public static void handle(OpenBankFromKeyBindPacketC2S packet, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        int slot = findBank(player);

        if (slot == -1)
            return;
        ItemStack bank = player.getInventory().getItem(slot);

        BankItemStorage bankItemStorage = Util.getBankItemStorage(bank);
        if (bankItemStorage == null)
            return;

        BankScreenHandlerFactory screenHandlerFactory = new BankScreenHandlerFactory(bankItemStorage.type, bankItemStorage, bank, slot, ContainerLevelAccess.NULL);
        player.openMenu(screenHandlerFactory, screenHandlerFactory::writeScreenOpeningData);
    }

    private static int findBank(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (Util.isBank(stack))
                return i;
            if (Util.isLink(stack) && Util.hasUUID(stack))
                return i;
        }
        return -1;
    }
}
