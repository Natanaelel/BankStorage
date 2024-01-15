package net.natte.bankstorage.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.natte.bankstorage.BankStorageClient;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;
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
            /* */
            ItemStack stack = right;//player.getStackInHand(packet.isRight ? Hand.MAIN_HAND : Hand.OFF_HAND);
            if (Util.isBankLike(stack) && Util.hasUUID(stack)) {
                CachedBankStorage bankItemStorage = CachedBankStorage.getBankStorage(stack);
                // BankItemStorage bankItemStorage = Util.getBankItemStorage(Util.getUUID(stack), player.getWorld());
                if (bankItemStorage == null)
                    return false;
                BankOptions options = Util.getOrCreateOptions(stack);
                options.selectedItemSlot -= (int) Math.signum(scroll);

                // int size = bankItemStorage.getBlockItems().size();
                int size = 0;
                for(ItemStack itemStack : bankItemStorage.items){
                    if(itemStack.getItem() instanceof BlockItem)size++;
                }
                options.selectedItemSlot = Math.max(Math.min(options.selectedItemSlot, size - 1), 0);
                player.sendMessage(Text.literal(""+options.selectedItemSlot));
                Util.setOptions(stack, options);
                // BuildModePreviewRenderer.Instance.setOptions(options);
                // BuildModePreviewRenderer.Instance.setBankStorage(bankItemStorage);
                // BuildModePreviewRenderer.Instance.stackInHand=stack;
                System.out.println(stack.hasNbt() ? stack.getNbt() : "no nbt");
                System.out.println(options.selectedItemSlot);
                BankStorageClient.buildModePreviewRenderer.setOptions(options);
                // BankStorageClient.buildModePreviewRenderer.stackInHand = stack;
                // BankStorageClient.buildModePreviewRenderer.updateBank();

                System.out.println("put options");



            }
            /* */
            return true;
        }

        if (isBankAndBuildMode(left)) {
            ClientPlayNetworking.send(new ScrollPacketC2S(false, scroll));
            return true;
        }

        return false;
    }

    // public static void useless(int ok){
    //     // if(ok == 0) return;
    //     System.out.println("useless lol");
    //     // return;
    // }
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
