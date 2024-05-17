package net.natte.bankstorage.rendering;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.natte.bankstorage.blockentity.BankDockBlockEntity;

public class BankDockBlockEntityRenderer implements BlockEntityRenderer<BankDockBlockEntity> {

    public BankDockBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(BankDockBlockEntity blockEntity, float tickDelta, MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumers, int light, int overlay) {

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        matrixStack.push();
        matrixStack.translate(0.5f, 0.5f, 0.5f);
        matrixStack.scale(2f, 2f, 2f);

        itemRenderer.renderItem(blockEntity.getBank(), ModelTransformationMode.FIXED, light, overlay, matrixStack,
                vertexConsumers, null, overlay);

        matrixStack.pop();
    }
}
