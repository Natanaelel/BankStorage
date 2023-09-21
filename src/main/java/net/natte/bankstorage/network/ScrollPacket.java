// package net.natte.bankstorage.network;

// import java.util.UUID;

// import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
// import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
// import net.minecraft.entity.player.PlayerEntity;
// import net.minecraft.network.PacketByteBuf;

// public class ScrollPacket {
//     public static void send(PlayerEntity player, UUID uuid, double scroll) {
//         PacketByteBuf buf = PacketByteBufs.create();
//         buf.writeUuid(uuid);
//         buf.writeDouble(scroll);
//         ClientPlayNetworking.send(OptionPackets.SCROLL_C2S_PACKET_ID, buf);
//     }
// }
