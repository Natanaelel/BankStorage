package net.natte.bankstorage.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.natte.bankstorage.network.OptionPackets;
// import net.natte.bankstorage.network.ScrollPacket;
import net.natte.bankstorage.util.Util;

@Environment(EnvType.CLIENT)
@Mixin(Mouse.class)
public class MouseMixin {

    @Redirect(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"))
    public void onScroll(PlayerInventory playerInventory, double scroll){
        PlayerEntity player = playerInventory.player;
        ItemStack itemStack = playerInventory.getMainHandStack();
        if(Util.isBank(itemStack) && player.isSneaking() ){
            // System.out.println("client? " + player.getWorld().isClient);
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeUuid(Util.getUUID(itemStack));
            buf.writeDouble(scroll);
            ClientPlayNetworking.send(OptionPackets.SCROLL_C2S_PACKET_ID, buf);
            
            // ScrollPacket.send(player, Util.getUUID(itemStack), scroll);
            return;
            // ClientPlayNetworking.send(OptionPackets.SCROLL_C2S_PACKET_ID, buf);
            // ClientPlay
            // ci.cancel();
        }
        playerInventory.scrollInHotbar(scroll);
    }
}
