package net.natte.bankstorage.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;

public class ItemStackBobbingAnimationPacketS2C implements FabricPacket {

    public static final PacketType<ItemStackBobbingAnimationPacketS2C> TYPE = PacketType
            .create(new Identifier(BankStorage.MOD_ID, "bobbing_s2c"), ItemStackBobbingAnimationPacketS2C::new);

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
