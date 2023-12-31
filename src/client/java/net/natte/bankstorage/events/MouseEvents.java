package net.natte.bankstorage.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.packet.server.ScrollPacketC2S;
import net.natte.bankstorage.util.Util;

@Environment(EnvType.CLIENT)
public class MouseEvents {

    public static boolean onScroll(PlayerInventory playerInventory, double scroll) {

        PlayerEntity player = playerInventory.player;

        if (!player.isSneaking())
            return false;

        ItemStack right = playerInventory.player.getMainHandStack();
        ItemStack left = playerInventory.player.getOffHandStack();

        if (isBankAndBuildMode(right)) {
            ClientPlayNetworking.send(new ScrollPacketC2S(true, scroll));
            return true;
        }

        if (isBankAndBuildMode(left)) {
            ClientPlayNetworking.send(new ScrollPacketC2S(false, scroll));
            return true;
        }

        return false;
    }

    private static boolean isBankAndBuildMode(ItemStack itemStack) {
        if (!Util.isBankLike(itemStack))
            return false;
        CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(itemStack);
        if (cachedBankStorage == null)
            return false;

        BuildMode buildMode = Util.getOrCreateOptions(itemStack).buildMode;

        return buildMode == BuildMode.NORMAL || buildMode == BuildMode.RANDOM;
    }
}
