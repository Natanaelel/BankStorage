package net.natte.bankstorage.packet.screensync;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.util.Util;

import java.util.List;

public class BankPacketHandler {

    // public static final Identifier toggle_pickup = new
    // Identifier(BankStorage.MOD_ID, "toggle_pickup");
    // public static final Identifier tag_mode = new Identifier(BankStorage.MOD_ID,
    // "tag_mode");
    // public static final Identifier sort = new Identifier(BankStorage.MOD_ID,
    // "sort");
    // public static final Identifier lock_slot = new Identifier(BankStorage.MOD_ID,
    // "lock_slot");

    // public static final Identifier pick_block = new
    // Identifier(BankStorage.MOD_ID, "pick_block");
    // public static final Identifier toggle_use = new
    // Identifier(BankStorage.MOD_ID, "toggle_use");
    // public static final Identifier scroll = new Identifier(BankStorage.MOD_ID,
    // "scroll");
    // public static final Identifier set_id = new Identifier(BankStorage.MOD_ID,
    // "set_id");
    // public static final Identifier lock_id = new Identifier(BankStorage.MOD_ID,
    // "lock_id");
    // public static final Identifier request_contents = new
    // Identifier(BankStorage.MOD_ID, "request_contents");

    public static final Identifier sync_slot = new Identifier(BankStorage.MOD_ID, "sync_slot");
    public static final Identifier sync_ghost = new Identifier(BankStorage.MOD_ID, "sync_ghost");
    public static final Identifier sync_container = new Identifier(BankStorage.MOD_ID, "sync_container");
    public static final Identifier sync_data = new Identifier(BankStorage.MOD_ID, "sync_data");
    public static final Identifier sync_inventory = new Identifier(BankStorage.MOD_ID, "sync_inventory");
    // public static final Identifier compress = new Identifier(BankStorage.MOD_ID,
    // "compress");

    // public static void registerMessages() {
        // ServerPlayNetworking.registerGlobalReceiver(scroll, new C2SMessageScrollSlot());
        // ServerPlayNetworking.registerGlobalReceiver(lock_slot, new C2SMessageLockSlot());
        // ServerPlayNetworking.registerGlobalReceiver(sort, new C2SMessageSort());
        // ServerPlayNetworking.registerGlobalReceiver(tag_mode, new C2SMessageTagMode());
        // ServerPlayNetworking.registerGlobalReceiver(toggle_pickup, new C2SMessageTogglePickup());
        // ServerPlayNetworking.registerGlobalReceiver(toggle_use, new C2SMessageToggleUseType());
        // ServerPlayNetworking.registerGlobalReceiver(pick_block, new C2SMessagePickBlock());
        // ServerPlayNetworking.registerGlobalReceiver(set_id, new C2SSetFrequencyPacket());
        // ServerPlayNetworking.registerGlobalReceiver(lock_id, new C2SMessageLockFrequency());
        // ServerPlayNetworking.registerGlobalReceiver(request_contents, new C2SRequestContentsPacket());
        // ServerPlayNetworking.registerGlobalReceiver(compress, new C2SMessageCompress());
    // }

    public static void sendSyncSlot(ServerPlayerEntity player, int id, int slot, ItemStack stack) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(id);
        buf.writeInt(slot);
        buf.writeNbt(Util.largeStackAsNbt(stack));
        ServerPlayNetworking.send(player, BankPacketHandler.sync_slot, buf);
    }

    public static void sendGhostItem(ServerPlayerEntity player, int id, int slot, ItemStack stack) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(id);
        buf.writeInt(slot);
        buf.writeNbt(Util.largeStackAsNbt(stack));
        ServerPlayNetworking.send(player, BankPacketHandler.sync_ghost, buf);
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

        ServerPlayNetworking.send(player, BankPacketHandler.sync_container, buf);
    }

    public static void sendSelectedItem(ServerPlayerEntity player, ItemStack stack) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeNbt(Util.largeStackAsNbt(stack));

        ServerPlayNetworking.send(player, BankPacketHandler.sync_data, buf);
    }

    public static void sendList(ServerPlayerEntity player, List<ItemStack> stacks) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(stacks.size());
        for (ItemStack stack : stacks) {
            buf.writeNbt(Util.largeStackAsNbt(stack));
        }
        ServerPlayNetworking.send(player, BankPacketHandler.sync_inventory, buf);
    }
}