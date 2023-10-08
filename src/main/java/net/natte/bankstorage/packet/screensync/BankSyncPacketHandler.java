package net.natte.bankstorage.packet.screensync;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.natte.bankstorage.util.Util;

public class BankSyncPacketHandler {

    public static final Identifier sync_slot = Util.ID("sync_slot");
    public static final Identifier sync_container = Util.ID("sync_container");

    public static void sendSyncSlot(ServerPlayerEntity player, int id, int slot, ItemStack stack) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(id);
        buf.writeInt(slot);
        buf.writeNbt(Util.largeStackAsNbt(stack));
        ServerPlayNetworking.send(player, BankSyncPacketHandler.sync_slot, buf);
    }

    public static void sendSyncContainer(ServerPlayerEntity player, int stateID, int containerID,
            DefaultedList<ItemStack> stacks, ItemStack carried) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(stateID);
        buf.writeInt(containerID);

        buf.writeItemStack(carried);

        buf.writeShort(stacks.size());

        for (ItemStack stack : stacks) {
            buf.writeNbt(Util.largeStackAsNbt(stack));

        }

        ServerPlayNetworking.send(player, BankSyncPacketHandler.sync_container, buf);
    }

    // public static void sendSyncLockedItem(ServerPlayerEntity player,)
}
