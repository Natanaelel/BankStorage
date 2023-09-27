// package net.natte.bankstorage.network.screensync;

// import org.jetbrains.annotations.Nullable;

// import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayChannelHandler;
// import net.fabricmc.fabric.api.networking.v1.PacketSender;
// import net.minecraft.client.MinecraftClient;
// import net.minecraft.client.network.ClientPlayNetworkHandler;
// import net.minecraft.client.network.ClientPlayerEntity;
// import net.minecraft.item.ItemStack;
// import net.minecraft.network.PacketByteBuf;
// import net.minecraft.util.collection.DefaultedList;
// import net.natte.bankstorage.screen.BankScreenHandler;
// import net.natte.bankstorage.util.Util;

// public class S2CSyncGhostItemPacket implements PlayChannelHandler {

//     public void handle(@Nullable ClientPlayerEntity player, int windowId, int slot, ItemStack stack) {
//         if (player != null && player.currentScreenHandler instanceof BankScreenHandler dankMenu && windowId == player.currentScreenHandler.syncId) {
//             dankMenu. .setGhostItem(slot,stack.getItem());
//         }
//     }

//     @Override
//     public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
//         int windowId = buf.readInt();
//         int slot = buf.readInt();
//         ItemStack stack = PacketBufferEX.readExtendedItemStack(buf);
//         client.execute(() -> handle(client.player, windowId, slot, stack));
//     }
// }