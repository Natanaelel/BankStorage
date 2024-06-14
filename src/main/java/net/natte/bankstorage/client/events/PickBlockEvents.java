package net.natte.bankstorage.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.client.BankStorageClient;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.packet.server.UpdateBankOptionsPacketC2S;
import net.natte.bankstorage.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PickBlockEvents {
    public static boolean pickBlock(ItemStack pickedStack) {
        Minecraft client = Minecraft.getInstance();
        ItemStack right = client.player.getMainHandItem();
        ItemStack left = client.player.getOffhandItem();
        return pickBlockFromBank(pickedStack, right, client) || pickBlockFromBank(pickedStack, left, client);
    }

    private static boolean pickBlockFromBank(ItemStack pickedStack, ItemStack heldStack, Minecraft client) {
        if (!Util.isBankLike(heldStack))
            return false;

        // BankOptions options = Util.getOrCreateOptions(heldStack);
        BankOptions options = BankStorageClient.buildModePreviewRenderer.options;
        if (options.buildMode != BuildMode.NORMAL)
            return false;

        CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(heldStack);
        if (cachedBankStorage == null)
            return false;

        ItemStack currentSelected = cachedBankStorage.getSelectedItem(options.selectedItemSlot);
        if (!currentSelected.isEmpty() && ItemStack.isSameItemSameComponents(pickedStack, currentSelected)) {
            return true;
        }
        int slot = -1;
        for (int i = 0; i < cachedBankStorage.blockItems.size(); ++i) {
            ItemStack itemStack = cachedBankStorage.blockItems.get(i);
            if (!itemStack.isEmpty() && ItemStack.isSameItemSameComponents(pickedStack, itemStack)) {
                slot = i;
                break;
            }
        }

        if (slot == -1)
            return false;

        options.selectedItemSlot = slot;
        client.getConnection().send(new UpdateBankOptionsPacketC2S(options));
        return true;

    }
}
