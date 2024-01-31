package net.natte.bankstorage.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.natte.bankstorage.BankStorageClient;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.packet.server.ScrollPacketC2S;
import net.natte.bankstorage.packet.server.UpdateBankOptionsPacketC2S;
import net.natte.bankstorage.rendering.BuildModePreviewRenderer;
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
            // ClientPlayNetworking.send(new ScrollPacketC2S(true, scroll,
            // BankStorageClient.buildModePreviewRenderer.nextRevision()));
            // /* */
            // ItemStack stack = right;//player.getStackInHand(packet.isRight ?
            // Hand.MAIN_HAND : Hand.OFF_HAND);
            // if (Util.isBankLike(stack) && Util.hasUUID(stack)) {
            // CachedBankStorage bankItemStorage = CachedBankStorage.getBankStorage(stack);
            // // BankItemStorage bankItemStorage =
            // Util.getBankItemStorage(Util.getUUID(stack), player.getWorld());
            // if (bankItemStorage == null)
            // return false;
            // BankOptions options = Util.getOrCreateOptions(stack);
            // options.selectedItemSlot -= (int) Math.signum(scroll);

            // // int size = bankItemStorage.getBlockItems().size();
            // int size = 0;
            // for(ItemStack itemStack : bankItemStorage.items){
            // if(itemStack.getItem() instanceof BlockItem)size++;
            // }
            // options.selectedItemSlot = Math.max(Math.min(options.selectedItemSlot, size -
            // 1), 0);
            // player.sendMessage(Text.literal(""+options.selectedItemSlot));
            // Util.setOptions(stack, options);
            // // BuildModePreviewRenderer.Instance.setOptions(options);
            // // BuildModePreviewRenderer.Instance.setBankStorage(bankItemStorage);
            // // BuildModePreviewRenderer.Instance.stackInHand=stack;
            // System.out.println(stack.hasNbt() ? stack.getNbt() : "no nbt");
            // System.out.println(options.selectedItemSlot);
            // BankStorageClient.buildModePreviewRenderer.setOptions(options);
            // // BankStorageClient.buildModePreviewRenderer.stackInHand = stack;
            // // BankStorageClient.buildModePreviewRenderer.updateBank();

            // System.out.println("put options");

            // }
            /* */

            ItemStack stack = right;
            BuildModePreviewRenderer preview = BankStorageClient.buildModePreviewRenderer;
            BankOptions options = preview.options;
            int selectedItemSlot = options.selectedItemSlot;
            selectedItemSlot -= (int) Math.signum(scroll);
            CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(stack);
            options.selectedItemSlot = MathHelper.clamp(selectedItemSlot, 0, cachedBankStorage.items.size() - 1);
            System.out.println(stack == preview.stackInHand);
            player.sendMessage(Text.of("selectedItemSlot " + selectedItemSlot));
            player.sendMessage(Text.of("new selected     " + options.selectedItemSlot));
            ClientPlayNetworking.send(new UpdateBankOptionsPacketC2S(options, preview.nextOptionsRevision()));

            return true;
        }

        if (isBankAndBuildMode(left)) {
            ClientPlayNetworking.send(
                    new ScrollPacketC2S(false, scroll, BankStorageClient.buildModePreviewRenderer.nextRevision()));
            return true;
        }

        return false;
    }

    public static void onToggleBuildMode(PlayerEntity player) {
        ItemStack stack;

        if (Util.isBankLike(player.getMainHandStack()))
            stack = player.getMainHandStack();
        else if (Util.isBankLike(player.getOffHandStack()))
            stack = player.getOffHandStack();
        else
            return;

        // BankOptions options = Util.getOrCreateOptions(stack);
        BankOptions options = BankStorageClient.buildModePreviewRenderer.options;
        options.buildMode = options.buildMode.next();
        Util.setOptions(stack, options);
        player.sendMessage(Text.translatable("popup.bankstorage.buildmode."
                + options.buildMode.toString().toLowerCase()), true);

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
