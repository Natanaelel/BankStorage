package net.natte.bankstorage.packet.client;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.natte.bankstorage.util.Util;

public class SyncedRandomPacketS2C implements FabricPacket {

    public static final PacketType<SyncedRandomPacketS2C> TYPE = PacketType
            .create(Util.ID("sync_random"), SyncedRandomPacketS2C::new);

    public long randomSeed;

    public SyncedRandomPacketS2C(long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public SyncedRandomPacketS2C(PacketByteBuf buf) {
        this.randomSeed = buf.readLong();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeLong(this.randomSeed);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
