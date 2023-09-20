package net.natte.bankstorage.item;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.natte.bankstorage.network.RequestBankStorage;
import net.natte.bankstorage.util.Util;

public class BuildModePreviewRenderer implements EndTick {

    public static final BuildModePreviewRenderer Instance = new BuildModePreviewRenderer();

    public List<ItemStack> stacks = new ArrayList<>();

    public ItemStack stackInHand;
    private MinecraftClient client;

    public BuildModePreviewRenderer(){
        this.client = MinecraftClient.getInstance();
        this.stackInHand = ItemStack.EMPTY;
    }

    public void render(DrawContext context, float tickDelta) {

    
        List<ItemStack> itemStacks = getItemStacks();
        int scaledHeight = context.getScaledWindowHeight();
        for (int i = 0; i < itemStacks.size(); ++i) {
            ItemStack itemStack = itemStacks.get(i);
            int m = scaledHeight - 16 - 3;
            // if (arm == Arm.LEFT) {
            renderHotbarItem(context, i - 91 - 26, m, tickDelta, null, itemStack, 0);
            // } else {
            // renderHotbarItem(context, i + 91 + 10, m, tickDelta, playerEntity, itemStack,
            // l++);
            // }
        }
        
    }

    private void renderHotbarItem(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack,
            int seed) {
        if (stack.isEmpty()) {
            return;
        }
        float g = (float) stack.getBobbingAnimationTime() - f;
        if (g > 0.0f) {
            float h = 1.0f + g / 5.0f;
            context.getMatrices().push();
            context.getMatrices().translate(x + 8, y + 12, 0.0f);
            context.getMatrices().scale(1.0f / h, (h + 1.0f) / 2.0f, 1.0f);
            context.getMatrices().translate(-(x + 8), -(y + 12), 0.0f);
        }
        // context.drawItem((LivingEntity) player, stack, x, y, seed);
        if (g > 0.0f) {
            context.getMatrices().pop();
        }
        context.drawItemInSlot(this.client.textRenderer, stack, x, y);
    }

    synchronized private List<ItemStack> getItemStacks(){
        return this.stacks;
    }

    // synchronized private void setItemStacks(List<ItemStack> stacks){
    //     this.stacks = stacks;
    // }

    public void onEndTick(MinecraftClient client){
        if(client.player == null) return;
        ItemStack stackInHand = client.player.getStackInHand(client.player.getActiveHand());
        if(stackInHand != this.stackInHand && Util.isBank(stackInHand)){
            this.stackInHand = stackInHand;
            RequestBankStorage.requestC2S(stackInHand);
        }
    }
}
