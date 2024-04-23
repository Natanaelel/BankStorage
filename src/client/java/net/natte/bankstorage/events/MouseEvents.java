package net.natte.bankstorage.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.natte.bankstorage.BankStorageClient;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.packet.server.UpdateBankOptionsPacketC2S;
import net.natte.bankstorage.rendering.BuildModePreviewRenderer;
import net.natte.bankstorage.util.Util;

@Environment(EnvType.CLIENT)
public class MouseEvents {

    public static boolean onScroll(PlayerInventory playerInventory, double scroll) {

        PlayerEntity player = playerInventory.player;

        if (!player.isSneaking())
            return false;

        ItemStack right = player.getMainHandStack();
        ItemStack left = player.getOffHandStack();
        ItemStack bank;
        if (Util.isBankLike(right))
            bank = right;
        else if (Util.isBankLike(left))
            bank = left;
        else
            return false;

        if (!isNormalBuildMode(bank))
            return false;

        BuildModePreviewRenderer preview = BankStorageClient.buildModePreviewRenderer;
        BankOptions options = preview.options;
        int selectedItemSlot = options.selectedItemSlot;
        selectedItemSlot -= (int) Math.signum(scroll);
        CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(bank);
        if (cachedBankStorage == null)
            return false;
        options.selectedItemSlot = MathHelper.clamp(selectedItemSlot, 0, cachedBankStorage.blockItems.size() - 1);
        ClientPlayNetworking.send(new UpdateBankOptionsPacketC2S(options));

        return true;

    }

    public static void onToggleBuildMode(PlayerEntity player) {

        // BankOptions options = Util.getOrCreateOptions(stack);
        BankOptions options = BankStorageClient.buildModePreviewRenderer.options;
        options.buildMode = options.buildMode.next();
        ClientPlayNetworking.send(new UpdateBankOptionsPacketC2S(options));
        // Util.setOptions(stack, options);
        player.sendMessage(Text.translatable("popup.bankstorage.buildmode."
                + options.buildMode.toString().toLowerCase()), true);

    }

    private static boolean isNormalBuildMode(ItemStack itemStack) {
        BuildMode buildMode = Util.getOrCreateOptions(itemStack).buildMode;

        return buildMode == BuildMode.NORMAL;
    }
}
