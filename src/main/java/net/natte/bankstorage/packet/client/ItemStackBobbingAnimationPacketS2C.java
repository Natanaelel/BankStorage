package net.natte.bankstorage.packet.client;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.natte.bankstorage.util.Util;

public class ItemStackBobbingAnimationPacketS2C implements FabricPacket {

    public static final PacketType<ItemStackBobbingAnimationPacketS2C> TYPE = PacketType
            .create(Util.ID("bobbing_s2c"), ItemStackBobbingAnimationPacketS2C::new);

    public int index;

    public ItemStackBobbingAnimationPacketS2C(int index) {
        this.index = index;
    }

    public ItemStackBobbingAnimationPacketS2C(PacketByteBuf buf) {
        this(buf.readInt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(this.index);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
