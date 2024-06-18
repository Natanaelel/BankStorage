package net.natte.bankstorage.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.client.BankStorageClient;
import net.natte.bankstorage.client.rendering.BuildModePreviewRenderer;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.packet.server.SelectedSlotPacketC2S;
import net.natte.bankstorage.packet.server.UpdateBankOptionsPacketC2S;
import net.natte.bankstorage.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.InputEvent;

@OnlyIn(Dist.CLIENT)
public class MouseEvents {

    public static void onScroll(InputEvent.MouseScrollingEvent event) {

        int scroll = -(int) Math.signum(event.getScrollDeltaY());

        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null)
            return;

        if (!player.isShiftKeyDown())
            return;

        BuildModePreviewRenderer preview = BankStorageClient.buildModePreviewRenderer;


        ItemStack bank = preview.getItem();

        if (bank == null)
            return;

        if (!Util.isBankLike(bank))
            return;

        if (!isNormalBuildMode(bank))
            return;

        CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(bank);

        if (cachedBankStorage == null)
            return;

        
        int selectedItemSlot = preview.selectedSlot;

        int newSelectedItemSlot = Mth.clamp(selectedItemSlot + scroll, 0, cachedBankStorage.getBlockItems().size() - 1);
        preview.selectedSlot = newSelectedItemSlot;

        player.connection.send(new SelectedSlotPacketC2S(preview.renderingFromHand == InteractionHand.MAIN_HAND, newSelectedItemSlot));

        event.setCanceled(true);
    }


    private static boolean isNormalBuildMode(ItemStack itemStack) {
        BuildMode buildMode = itemStack.getOrDefault(BankStorage.OptionsComponentType, BankOptions.DEFAULT).buildMode();

        return buildMode == BuildMode.NORMAL;
    }
}
