package net.natte.bankstorage.network.screensync;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayChannelHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.natte.bankstorage.util.Util;

public class S2CSyncSelected implements PlayChannelHandler {

    public void handle(@Nullable ClientPlayerEntity player, ItemStack stack) {
        if (player != null) {
            // TODO:
            // ClientData.setData(stack);
        }
    }

    @Override
    public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        ItemStack stack = Util.largeStackFromNbt(buf.readNbt());
        client.execute(() -> handle(client.player, stack));
    }
}