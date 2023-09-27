package net.natte.bankstorage.packet;

import java.util.UUID;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPacketHandler;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;

public class OptionPacketS2C
        implements FabricPacket {

    public static final PacketType<OptionPacketS2C> TYPE = PacketType
            .create(new Identifier(BankStorage.MOD_ID, "options"), OptionPacketS2C::new);

    public static class Receiver implements PlayPacketHandler<OptionPacketS2C> {

        public void receive(OptionPacketS2C packet, ClientPlayerEntity player,
                PacketSender responseSender) {
            CachedBankStorage cachedBankStorage = CachedBankStorage.BANK_CACHE.get(packet.uuid);
            if (cachedBankStorage != null) {
                cachedBankStorage.options = BankOptions.fromNbt(packet.nbt);
            }

        }
    }

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