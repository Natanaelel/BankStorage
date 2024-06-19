package net.natte.bankstorage.packet.server;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.blockentity.BankDockBlockEntity;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

public record PickupModePacketC2S() implements CustomPacketPayload {

    public static final PickupModePacketC2S INSTANCE = new PickupModePacketC2S();

    public static final CustomPacketPayload.Type<PickupModePacketC2S> TYPE = new Type<>(Util.ID("toggle_pickupmode_c2s"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PickupModePacketC2S> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<PickupModePacketC2S> type() {
        return TYPE;
    }

    public static void handle(PickupModePacketC2S packet, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof BankScreenHandler bankScreenHandler)
            togglePickupModeOfScreenHandler(bankScreenHandler);
        else
            togglePickupModeOfHeldBank(player);

    }

    private static void togglePickupModeOfScreenHandler(BankScreenHandler bankScreenHandler) {

        ItemStack bank = bankScreenHandler.getBankLikeItem();

        bank.update(BankStorage.OptionsComponentType, BankOptions.DEFAULT, BankOptions::nextPickupMode);

        // dock.markDirty if has dock pos
        bankScreenHandler.getContext().execute(
                (world, blockPos) -> world
                        .getBlockEntity(blockPos, BankStorage.BANK_DOCK_BLOCK_ENTITY.get())
                        .ifPresent(BankDockBlockEntity::setChanged));
    }

    private static void togglePickupModeOfHeldBank(ServerPlayer player) {
        ItemStack bank = getBankLike(player);
        if (bank == null)
            return;

        bank.update(BankStorage.OptionsComponentType, BankOptions.DEFAULT, BankOptions::nextPickupMode);

        player.displayClientMessage(Component.translatable("popup.bankstorage.pickupmode."
                + bank.get(BankStorage.OptionsComponentType).pickupMode().toString().toLowerCase()), true);
    }

    @Nullable
    private static ItemStack getBankLike(Player player) {
        if (Util.isBankLike(player.getMainHandItem()))
            return player.getMainHandItem();
        if (Util.isBankLike(player.getOffhandItem()))
            return player.getOffhandItem();
        return null;
    }
}
