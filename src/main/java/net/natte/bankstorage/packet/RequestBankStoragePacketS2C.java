package net.natte.bankstorage.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.util.Util;

public class RequestBankStoragePacketS2C implements FabricPacket {

    public static final PacketType<RequestBankStoragePacketS2C> TYPE = PacketType
            .create(new Identifier(BankStorage.MOD_ID, "requestbank_s2c"), RequestBankStoragePacketS2C::new);

    public CachedBankStorage cachedBankStorage;
    public long randomSeed;

    public RequestBankStoragePacketS2C(CachedBankStorage cachedBankStorage, long randomSeed) {
        this.cachedBankStorage = cachedBankStorage;
        this.randomSeed = randomSeed;
    }

    public RequestBankStoragePacketS2C(PacketByteBuf buf) {
        int size = buf.readInt();
        List<ItemStack> items = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            items.add(Util.readLargeStack(buf));
        }
        UUID uuid = buf.readUuid();
        Long randomSeed = buf.readLong();
        BankOptions options = BankOptions.fromNbt(buf.readNbt());

        this.cachedBankStorage = new CachedBankStorage(items, uuid, options, randomSeed);

        // never used, just a reminder. this.randomSeed = randomSeed;
    }

    @Override
    public void write(PacketByteBuf buf) {

        buf.writeInt(this.cachedBankStorage.items.size());
        for (ItemStack itemStack : this.cachedBankStorage.items) {
            Util.writeLargeStack(buf, itemStack);
        }
        buf.writeUuid(this.cachedBankStorage.uuid);
        buf.writeLong(this.randomSeed);
        buf.writeNbt(this.cachedBankStorage.options.asNbt());

    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
