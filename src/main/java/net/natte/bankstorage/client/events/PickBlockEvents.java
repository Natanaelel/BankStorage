package net.natte.bankstorage.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.client.BankStorageClient;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.packet.server.SelectedSlotPacketC2S;
import net.natte.bankstorage.packet.server.UpdateBankOptionsPacketC2S;
import net.natte.bankstorage.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PickBlockEvents {
    public static boolean pickBlock(ItemStack pickedStack) {
        Minecraft client = Minecraft.getInstance();
        return pickBlockFromBank(pickedStack, InteractionHand.MAIN_HAND, client) || pickBlockFromBank(pickedStack, InteractionHand.OFF_HAND, client);
    }

    private static boolean pickBlockFromBank(ItemStack pickedStack, InteractionHand hand, Minecraft client) {
        ItemStack bankItem = client.player.getItemInHand(hand);
        if (!Util.isBankLike(bankItem))
            return false;

        BankOptions options = bankItem.getOrDefault(BankStorage.OptionsComponentType, BankOptions.DEFAULT);
        if (options.buildMode() != BuildMode.NORMAL)
            return false;

        CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(bankItem);
        if (cachedBankStorage == null)
            return false;

        int selectedSlot = bankItem.getOrDefault(BankStorage.SelectedSlotComponentType, 0);
        ItemStack currentSelected = cachedBankStorage.getSelectedItem(selectedSlot);
        if (!currentSelected.isEmpty() && ItemStack.isSameItemSameComponents(pickedStack, currentSelected)) {
            return true;
        }
        int slot = -1;
        for (int i = 0; i < cachedBankStorage.getBlockItems().size(); ++i) {
            ItemStack itemStack = cachedBankStorage.getBlockItems().get(i);
            if (!itemStack.isEmpty() && ItemStack.isSameItemSameComponents(pickedStack, itemStack)) {
                slot = i;
                break;
            }
        }

        if (slot == -1)
            return false;
        
        BankStorageClient.buildModePreviewRenderer.selectedSlot = slot;

        client.getConnection().send(new SelectedSlotPacketC2S(hand == InteractionHand.MAIN_HAND, slot));
        return true;

    }
}
