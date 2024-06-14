package net.natte.bankstorage.packet.server;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PickupModePacketC2S() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PickupModePacketC2S> TYPE = new Type<>(Util.ID("pickupmode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PickupModePacketC2S> STREAM_CODEC = StreamCodec.unit(new PickupModePacketC2S());

    @Override
    public Type<PickupModePacketC2S> type() {
        return TYPE;
    }

    public static void handle(PickupModePacketC2S packet, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof BankScreenHandler bankScreenHandler)
            togglePickupModeOfScreenHandler(player, bankScreenHandler);
        else
            togglePickupModeOfHeldBank(player);

    }

    private static void togglePickupModeOfScreenHandler(ServerPlayer player, BankScreenHandler bankScreenHandler) {
        ItemStack bank = bankScreenHandler.getBankLikeItem();
        BankOptions options = Util.getOrCreateOptions(bank);
        options.pickupMode = options.pickupMode.next();
        Util.setOptions(bank, options);

        // dock.markDirty if has dock pos
        bankScreenHandler.getContext().execute(
                (world, blockPos) -> world
                        .getBlockEntity(blockPos, BankStorage.BANK_DOCK_BLOCK_ENTITY.get())
                        .ifPresent(dock -> {
                            if (dock.hasBank()) {
                                Util.setOptions(dock.getBank(), options);
                                dock.setChanged();
                            }
                        }));
    }

    private static void togglePickupModeOfHeldBank(ServerPlayer player) {
        ItemStack stack;
        if (Util.isBankLike(player.getMainHandItem()))
            stack = player.getOffhandItem();
        else if (Util.isBankLike(player.getOffhandItem()))
            stack = player.getOffhandItem();
        else
            return;

        BankOptions options = Util.getOrCreateOptions(stack);
        options.pickupMode = options.pickupMode.next();
        Util.setOptions(stack, options);
        player.displayClientMessage(Component.translatable("popup.bankstorage.pickupmode."
                + options.pickupMode.toString().toLowerCase()), true);
    }
}
