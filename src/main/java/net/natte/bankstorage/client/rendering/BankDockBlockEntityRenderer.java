package net.natte.bankstorage.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.natte.bankstorage.blockentity.BankDockBlockEntity;

public class BankDockBlockEntityRenderer implements BlockEntityRenderer<BankDockBlockEntity> {

    private final ItemRenderer itemRenderer;

    public BankDockBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(BankDockBlockEntity blockEntity, float tickDelta, PoseStack poseStack, MultiBufferSource vertexConsumers, int packedLight, int packedOverlay) {

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.scale(2f, 2f, 2f);

        itemRenderer.renderStatic(blockEntity.getBank(), ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack,
                vertexConsumers, null, 0);

        poseStack.popPose();
    }
}
