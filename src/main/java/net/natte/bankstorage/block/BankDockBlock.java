package net.natte.bankstorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.natte.bankstorage.blockentity.BankDockBlockEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.screen.BankScreenHandlerFactory;
import net.natte.bankstorage.util.Util;
import org.jetbrains.annotations.Nullable;

public class BankDockBlock extends Block implements EntityBlock {

    public BankDockBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BankDockBlockEntity(pos, state);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {

        BlockEntity blockEntity = world.getBlockEntity(pos);
        InteractionHand hand = player.getUsedItemHand();
        if (blockEntity instanceof BankDockBlockEntity bankDockBlockEntity) {
            ItemStack stackInHand = player.getItemInHand(hand);
            if (bankDockBlockEntity.hasBank()) {

                // pick up bank from dock
                if (stackInHand.isEmpty() && player.isShiftKeyDown()) {
                    ItemStack bankInDock = bankDockBlockEntity.pickUpBank();
                    bankInDock.setPopTime(5);
                    player.setItemInHand(hand, bankInDock);

                    world.playSound(null, player, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f,
                            (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 1.4f + 2.0f);
                    return InteractionResult.FAIL;
                }

                // swap hand and dock
                if (Util.isBankLike(stackInHand)) {
                    player.setItemInHand(hand, ItemStack.EMPTY);
                    ItemStack bankInDock = bankDockBlockEntity.pickUpBank();
                    bankInDock.setPopTime(5);
                    player.setItemInHand(hand, bankInDock);
                    world.playSound(null, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f,
                            SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f, 0.0f);
                    world.playSound(null, player, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f,
                            (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 1.4f + 2.0f);

                    bankDockBlockEntity.putBank(stackInHand);
                    return InteractionResult.SUCCESS;
                }

                // open bank screen
                if (!world.isClientSide) {
                    ItemStack bankItem = bankDockBlockEntity.getBank();
                    BankItemStorage bankItemStorage = Util.getBankItemStorage(bankItem);
                    if (bankItemStorage == null)
                        return InteractionResult.FAIL;
                    BankScreenHandlerFactory screenHandlerFactory = new BankScreenHandlerFactory(
                            bankItemStorage.type(),
                            bankItemStorage,
                            bankItem,
                            -1,
                            ContainerLevelAccess.create(world, pos)
                    );
                    player.openMenu(screenHandlerFactory, screenHandlerFactory::writeScreenOpeningData);
                }

                return InteractionResult.SUCCESS;
            } else {
                // place bank in dock
                if (Util.isBankLike(stackInHand)) {
                    bankDockBlockEntity.putBank(player.getItemInHand(hand));
                    player.setItemInHand(hand, ItemStack.EMPTY);
                    world.playSound(null, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f,
                            SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f, 0.0f);

                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.is(newState.getBlock())) {
            return;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BankDockBlockEntity bankDockBlockEntity) {
            Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), bankDockBlockEntity.getBank());
        }
        super.onRemove(state, world, pos, newState, moved);
    }
}
