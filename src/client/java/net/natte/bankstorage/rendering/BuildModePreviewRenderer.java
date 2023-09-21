package net.natte.bankstorage.rendering;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.network.RequestBankStorage;
import net.natte.bankstorage.util.Util;

public class BuildModePreviewRenderer implements EndTick {

    public static final BuildModePreviewRenderer Instance = new BuildModePreviewRenderer();

    public List<ItemStack> stacks = new ArrayList<>();

    public ItemStack stackInHand;
    public UUID uuid;
    private MinecraftClient client;

    private CachedBankStorage bankStorage;

    public BuildModePreviewRenderer() {
        this.client = MinecraftClient.getInstance();
        this.stackInHand = ItemStack.EMPTY;
    }

    public void render(DrawContext context, float tickDelta) {

        // if (!Util.isBank(this.stackInHand))
        // return;
        if (this.bankStorage == null)
            return;
        if (this.bankStorage.items.isEmpty())
            return;
        List<ItemStack> itemStacks = this.bankStorage.items;
        int scaledHeight = context.getScaledWindowHeight();
        int scaledWidth = context.getScaledWindowWidth();

        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        context.drawTexture(new Identifier(BankStorage.MOD_ID, "textures/gui/widgets.png"),
                scaledWidth / 2 - 90 - 29 - 29 - Math.min(itemStacks.size(), 9) * 20 - 4 + 23, scaledHeight - 22, 0, 22,
                19 + itemStacks.size() * 20 - 20, 22);
        matrixStack.pop();
        matrixStack.push();
        context.drawTexture(new Identifier(BankStorage.MOD_ID, "textures/gui/widgets.png"),
                scaledWidth / 2 - 90 - 29 - 29 - 2 - 3 + 23, scaledHeight - 22, 179, 22, 3, 22);
        matrixStack.pop();
        // context.drawTexture(new Identifier(BankStorage.MOD_ID,
        // "textures/gui/widgets.png"), scaledWidth / 2 - 90 - 29 - 29 -
        // itemStacks.size() * 20 + 2 - 3 + 20, scaledHeight - 22, 0, 22, 11, 22);

        for (int i = 0; i < Math.min(itemStacks.size(), 9); ++i) {
            ItemStack itemStack = itemStacks.get(i);
            int y = scaledHeight - 16 - 3;
            int x = scaledWidth / 2 - 90 - 29 - 29 - i * 20 + 2;
            // x = scaledWidth / 2 - 91 - 26;
            // x = scaledWidth / 2 - 90 + 2 - 29 ;
            // if (arm == Arm.LEFT) {
            // System.out.println(itemStack);
            // context.drawTexture(new Identifier(BankStorage.MOD_ID,
            // "textures/gui/widgets.png"), x - 3, scaledHeight - 22, 0, 0, 22, 22);
            // context.drawTexture(new Identifier("textures/gui/widgets.png"), x - 3, y - 3,
            // 24, 22, 29, 24);

            renderHotbarItem(context, x, y, tickDelta, client.player, itemStack, 0);
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
        context.drawItem((LivingEntity) player, stack, x, y, seed);
        if (g > 0.0f) {
            context.getMatrices().pop();
        }
        context.drawItemInSlot(this.client.textRenderer, stack, x, y);
    }

    synchronized public List<ItemStack> getItemStacks(ItemStack bank) {

        return this.stacks;

    }

    synchronized public void setItemStacks(List<ItemStack> stacks) {
        this.stacks = stacks;
    }

    public void onEndTick(MinecraftClient client) {
        if (client.player == null)
            return;
        ItemStack stackInHand = client.player.getStackInHand(client.player.getActiveHand());

        if (Util.isBank(stackInHand)) {
            if (stackInHand != this.stackInHand) {
                clearBank();
                this.stackInHand = stackInHand;
                if (Util.hasUUID(this.stackInHand)) {
                    this.setUUID(Util.getUUID(this.stackInHand));
                }
                updateBank();
                // setBankStorage(CachedBankStorage.getBankStorage(this.stackInHand));
            }
        } else {
            this.stackInHand = ItemStack.EMPTY;
            this.bankStorage = null;
            this.uuid = null;
        }
    }

    public void setBankStorage(CachedBankStorage bankStorage) {
        this.bankStorage = bankStorage;
    }

    public void updateBank() {
        // System.out.println("update bank");
        setBankStorage(CachedBankStorage.getBankStorage(this.stackInHand));
    }

    public void clearBank() {
        this.uuid = null;
        this.bankStorage = null;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }
}
