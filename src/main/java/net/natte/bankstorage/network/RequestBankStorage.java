package net.natte.bankstorage.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;

@Environment(EnvType.CLIENT)
public class RequestBankStorage {
    
    public static Identifier C2S_PACKET_ID = new Identifier(BankStorage.MOD_ID, "requeststorage_c2s");
    public static Identifier S2C_PACKET_ID = new Identifier(BankStorage.MOD_ID, "requeststorage_s2c");

    public static void requestC2S(ItemStack itemStack){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeItemStack(itemStack);
        ClientPlayNetworking.send(C2S_PACKET_ID, buf);
    }

    public static PacketByteBuf createPacketS2C(List<ItemStack> items, int selectedItemSlot, UUID uuid, BankOptions options, ItemStack bank){
        PacketByteBuf buf =  PacketByteBufs.create();
        buf.writeInt(items.size());
        for(ItemStack itemStack : items){
            buf.writeItemStack(itemStack);
        }
        buf.writeInt(selectedItemSlot);
        buf.writeUuid(uuid);
        buf.writeItemStack(bank);
        options.writeToPacketByteBuf(buf);
        return buf;
    }

    public static CachedBankStorage readPacketS2C(PacketByteBuf buf){
        int size = buf.readInt();
        List<ItemStack> items = new ArrayList<>(size);
        for(int i = 0; i < size; ++i){
            items.add(buf.readItemStack());
        }
        int selectedItemSlot = buf.readInt();
        UUID uuid = buf.readUuid();
        ItemStack bank = buf.readItemStack();
        BankOptions options = BankOptions.readPacketByteBuf(buf);

        return new CachedBankStorage(items, selectedItemSlot, uuid, options, bank);
    }
}
