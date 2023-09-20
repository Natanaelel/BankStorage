package net.natte.bankstorage.network;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;

public class PreviewItemsS2C {
    public static Identifier PACKED_ID = new Identifier(BankStorage.MOD_ID, "previewitems");

    public static PacketByteBuf createPacket(List<ItemStack> items){
        PacketByteBuf buf =  PacketByteBufs.create();
        buf.writeInt(items.size());
        for(ItemStack itemStack : items){
            buf.writeItemStack(itemStack);
        }
        return buf;
    }

    public static List<ItemStack> frompacket(PacketByteBuf buf){
        int size = buf.readInt();
        List<ItemStack> items = new ArrayList<>(size);
        for(int i = 0; i < size; i++){
            items.add(buf.readItemStack());
        }
        return items;
    }
}
