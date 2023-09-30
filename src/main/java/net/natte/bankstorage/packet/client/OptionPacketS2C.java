package net.natte.bankstorage.packet.client;

import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.natte.bankstorage.util.Util;

public class OptionPacketS2C implements FabricPacket {

    public static final PacketType<OptionPacketS2C> TYPE = PacketType
            .create(Util.ID("options_s2c"), OptionPacketS2C::new);

    public UUID uuid;
    public NbtCompound nbt;

    public OptionPacketS2C(UUID uuid, NbtCompound nbt) {
        this.uuid = uuid;
        this.nbt = nbt;
    }

    public OptionPacketS2C(PacketByteBuf buf) {
        this(buf.readUuid(), buf.readNbt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.uuid);
        buf.writeNbt(this.nbt);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
