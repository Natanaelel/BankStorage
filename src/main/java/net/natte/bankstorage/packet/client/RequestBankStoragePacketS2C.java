package net.natte.bankstorage.packet.client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.util.Util;

public class RequestBankStoragePacketS2C implements CustomPayload {

    // public static final PacketType<RequestBankStoragePacketS2C> TYPE = PacketType
            // .create(Util.ID("requestbank_s2c"), RequestBankStoragePacketS2C::new);

    public static final CustomPayload.Id<RequestBankStoragePacketS2C> PACKET_ID = new CustomPayload.Id<>(Util.ID("requestbank_s2c"));
    public static final PacketCodec<PacketByteBuf, RequestBankStoragePacketS2C> PACKET_CODEC = PacketCodec.of(RequestBankStoragePacketS2C::write, RequestBankStoragePacketS2C::new);


    public CachedBankStorage cachedBankStorage;

    public RequestBankStoragePacketS2C(CachedBankStorage cachedBankStorage) {
        this.cachedBankStorage = cachedBankStorage;
    }

    public RequestBankStoragePacketS2C(PacketByteBuf buf) {
        int size = buf.readInt();
        List<ItemStack> items = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            items.add(Util.readLargeStack(buf));
        }
        UUID uuid = buf.readUuid();
        short revision = buf.readShort();
        short optionsRevision = buf.readShort();

        this.cachedBankStorage = new CachedBankStorage(items, uuid, revision, optionsRevision);

        // never used here, just a reminder. this.randomSeed = randomSeed;
        // no idea what ^that^ means, but I'll keep it as a reminder
    }

    // @Override
    public void write(PacketByteBuf buf) {

        buf.writeInt(this.cachedBankStorage.items.size());
        for (ItemStack itemStack : this.cachedBankStorage.items) {
            Util.writeLargeStack(buf, itemStack);
        }
        buf.writeUuid(this.cachedBankStorage.uuid);
        buf.writeShort(this.cachedBankStorage.revision);
        buf.writeShort(this.cachedBankStorage.revision);

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
