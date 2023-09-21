package net.natte.bankstorage.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.util.Util;

public class RequestBankStorage {
    
    public static final Identifier C2S_PACKET_ID = new Identifier(BankStorage.MOD_ID, "requeststorage_c2s");
    public static final Identifier S2C_PACKET_ID = new Identifier(BankStorage.MOD_ID, "requeststorage_s2c");

    public static PacketByteBuf createRequestC2S(UUID uuid){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(uuid);
        return buf;
    }

    public static PacketByteBuf createPacketS2C(List<ItemStack> items, UUID uuid, BankOptions options, long randomSeed){
        PacketByteBuf buf =  PacketByteBufs.create();
        buf.writeInt(items.size());
        for(ItemStack itemStack : items){
            buf.writeNbt(Util.largeStackAsNbt(itemStack));
        }
        buf.writeUuid(uuid);
        buf.writeLong(randomSeed);
        buf.writeNbt(options.asNbt());
        return buf;
    }

    public static CachedBankStorage readPacketS2C(PacketByteBuf buf){
        int size = buf.readInt();
        List<ItemStack> items = new ArrayList<>(size);
        for(int i = 0; i < size; ++i){
            items.add(Util.largeStackFromNbt(buf.readNbt()));
        }
        UUID uuid = buf.readUuid();
        Long randomSeed = buf.readLong();
        BankOptions options = BankOptions.fromNbt(buf.readNbt());

        return new CachedBankStorage(items, uuid, options, randomSeed);
    }
}
