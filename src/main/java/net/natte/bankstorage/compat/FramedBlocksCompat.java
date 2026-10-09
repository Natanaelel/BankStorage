package net.natte.bankstorage.compat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.fml.ModList;
import xfacthd.framedblocks.api.block.blockentity.FramedBlockEntity;

public class FramedBlocksCompat {
    public static boolean loaded = false;

    public static void initialize() {
        loaded = ModList.get().isLoaded("framedblocks");
    }


    public static InteractionResult handleInteractionIfIsFramedBlock(BlockEntity blockEntity, Player player, InteractionHand hand, BlockHitResult hit) {
        if (loaded && blockEntity instanceof FramedBlockEntity framedBlockEntity) {
            return framedBlockEntity.handleInteraction(player, hand, hit).result();
        }
        return InteractionResult.FAIL;
    }
}
