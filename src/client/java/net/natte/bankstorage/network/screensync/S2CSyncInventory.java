package net.natte.bankstorage.network.screensync;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayChannelHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.natte.bankstorage.util.Util;


public class S2CSyncInventory implements PlayChannelHandler {

    public void handle(@Nullable ClientPlayerEntity player, List<ItemStack> stacks) {
        if (player != null) {
            // TODO:
            // ClientData.setList(stacks);
        }
    }

    @Override
    public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        int length = buf.readInt();
        List<ItemStack> stacks = new ArrayList<>(length);
        for(int i = 0; i < length; ++i){
            stacks.add(Util.largeStackFromNbt(buf.readNbt()));
        }
        client.execute(() -> handle(client.player, stacks));
    }
}