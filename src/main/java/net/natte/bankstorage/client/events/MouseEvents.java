package net.natte.bankstorage.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.natte.bankstorage.client.BankStorageClient;
import net.natte.bankstorage.client.rendering.BuildModePreviewRenderer;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.packet.server.UpdateBankOptionsPacketC2S;
import net.natte.bankstorage.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.InputEvent;

@OnlyIn(Dist.CLIENT)
public class MouseEvents {

    //    public static boolean onScroll(Inventory playerInventory, double scroll) {
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (!player.isShiftKeyDown())
            return;

        ItemStack right = player.getMainHandItem();
        ItemStack left = player.getOffhandItem();
        ItemStack bank;
        if (Util.isBankLike(right))
            bank = right;
        else if (Util.isBankLike(left))
            bank = left;
        else
            return;

        if (!isNormalBuildMode(bank))
            return;

        BuildModePreviewRenderer preview = BankStorageClient.buildModePreviewRenderer;
        BankOptions options = preview.options;
        int selectedItemSlot = options.selectedItemSlot;
        int newSelectedItemSlot = selectedItemSlot - (int) Math.signum(event.getScrollDeltaY());
        CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(bank);
        if (cachedBankStorage == null)
            return;
        newSelectedItemSlot = Mth.clamp(newSelectedItemSlot, 0, cachedBankStorage.blockItems.size() - 1);
        if (newSelectedItemSlot != selectedItemSlot) {
            options.selectedItemSlot = newSelectedItemSlot;
            player.connection.send(new UpdateBankOptionsPacketC2S(options));
            event.setCanceled(true);
        }

    }

    public static void onToggleBuildMode(Player player) {

        // BankOptions options = Util.getOrCreateOptions(stack);
        BankOptions options = BankStorageClient.buildModePreviewRenderer.options;
        options.buildMode = options.buildMode.next();
        ((LocalPlayer) player).connection.send(new UpdateBankOptionsPacketC2S(options));
        // Util.setOptions(stack, options);
        player.displayClientMessage(Component.translatable("popup.bankstorage.buildmode."
                + options.buildMode.toString().toLowerCase()), true);

    }

    private static boolean isNormalBuildMode(ItemStack itemStack) {
        BuildMode buildMode = Util.getOrCreateOptions(itemStack).buildMode;

        return buildMode == BuildMode.NORMAL;
    }
}
