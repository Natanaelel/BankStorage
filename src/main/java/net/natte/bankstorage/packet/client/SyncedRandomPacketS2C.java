package net.natte.bankstorage.packet.client;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.natte.bankstorage.util.Util;

public class SyncedRandomPacketS2C implements CustomPayload {

    // public static final PacketType<SyncedRandomPacketS2C> TYPE = PacketType
    //         .create(Util.ID("sync_random"), SyncedRandomPacketS2C::new);
    public static final CustomPayload.Id<SyncedRandomPacketS2C> PACKET_ID = new CustomPayload.Id<>(Util.ID("sync_random"));
    public static final PacketCodec<RegistryByteBuf, SyncedRandomPacketS2C> PACKET_CODEC = PacketCodec.of(SyncedRandomPacketS2C::write, SyncedRandomPacketS2C::new);
   
    public long randomSeed;

    public SyncedRandomPacketS2C(long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public SyncedRandomPacketS2C(PacketByteBuf buf) {
        this.randomSeed = buf.readLong();
    }

    // @Override
    public void write(PacketByteBuf buf) {
        buf.writeLong(this.randomSeed);
    }

    // @Override
    // public PacketType<?> getType() {
    //     return TYPE;
    // }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
