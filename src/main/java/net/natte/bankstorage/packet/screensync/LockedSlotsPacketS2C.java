package net.natte.bankstorage.packet.screensync;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LockedSlotsPacketS2C(int containerId,
                                   Map<Integer, ItemStack> lockedSlots) implements CustomPacketPayload {

    public static final Type<LockedSlotsPacketS2C> TYPE = new Type<>(Util.ID("sync_locked_slots_s2c"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LockedSlotsPacketS2C> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            LockedSlotsPacketS2C::containerId,
            ByteBufCodecs.map(HashMap::newHashMap, ByteBufCodecs.INT, ItemStack.OPTIONAL_STREAM_CODEC),
            LockedSlotsPacketS2C::lockedSlots,
            LockedSlotsPacketS2C::new);

    @Override
    public Type<LockedSlotsPacketS2C> type() {
        return TYPE;
    }


    public static void handle(LockedSlotsPacketS2C packet, IPayloadContext context) {
        LocalPlayer player = (LocalPlayer) context.player();
        if (player.containerMenu.containerId != packet.containerId())
            return;
        if (!(player.containerMenu instanceof BankScreenHandler bankScreenHandler))
            return;
        bankScreenHandler.setLockedSlotsNoSync(packet.lockedSlots());
    }
}
