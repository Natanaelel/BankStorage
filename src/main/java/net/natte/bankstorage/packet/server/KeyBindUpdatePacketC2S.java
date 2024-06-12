package net.natte.bankstorage.packet.server;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record KeyBindUpdatePacketC2S(boolean isUnbound) implements CustomPacketPayload {

    public static final Type<KeyBindUpdatePacketC2S> TYPE = new Type<>(Util.ID("keybindupdate"));
    public static final StreamCodec<RegistryFriendlyByteBuf, KeyBindUpdatePacketC2S> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            KeyBindUpdatePacketC2S::isUnbound,
            KeyBindUpdatePacketC2S::new);

    @Override
    public Type<KeyBindUpdatePacketC2S> type() {
        return TYPE;
    }

    public static void handle(KeyBindUpdatePacketC2S packet, IPayloadContext context) {
        Util.isBuildModeKeyUnBound = packet.isUnbound;
    }
}
