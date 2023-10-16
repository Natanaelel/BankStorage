package net.natte.bankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.blockentity.BankDockBlockEntity;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.PickupMode;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;

public class PickupModePacketC2S implements FabricPacket {

    public static final PacketType<PickupModePacketC2S> TYPE = PacketType
            .create(Util.ID("pickupmode"), PickupModePacketC2S::new);

    public static class Receiver implements
            PlayPacketHandler<PickupModePacketC2S> {

        @Override
        public void receive(PickupModePacketC2S packet, ServerPlayerEntity player,
                PacketSender responseSender) {
            if (player.currentScreenHandler instanceof BankScreenHandler bankScreenHandler) {
                BankOptions options = Util.getOrCreateOptions(bankScreenHandler.getBankLikeItem());
                options.pickupMode = packet.pickupMode;
                Util.setOptions(bankScreenHandler.getBankLikeItem(), options);
                bankScreenHandler.getContext().run(
                        (world, blockPos) -> world
                                .getBlockEntity(blockPos, BankStorage.BANK_DOCK_BLOCK_ENTITY)
                                .ifPresent(BankDockBlockEntity::markDirty));

            } else if (Util.isBankLike(player.getMainHandStack())) {

                BankOptions options = Util.getOrCreateOptions(player.getMainHandStack());
                options.pickupMode = packet.pickupMode;
                Util.setOptions(player.getMainHandStack(), options);
            
            } else if (Util.isBankLike(player.getOffHandStack())) {
            
                BankOptions options = Util.getOrCreateOptions(player.getOffHandStack());
                options.pickupMode = packet.pickupMode;
                Util.setOptions(player.getOffHandStack(), options);
            
            }
        }
    }

    public PickupMode pickupMode;

    public PickupModePacketC2S(PickupMode pickupMode) {
        this.pickupMode = pickupMode;
    }

    public PickupModePacketC2S(PacketByteBuf buf) {
        this(PickupMode.from(buf.readByte()));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByte(pickupMode.number);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
