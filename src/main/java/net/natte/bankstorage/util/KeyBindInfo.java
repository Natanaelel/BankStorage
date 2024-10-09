package net.natte.bankstorage.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record KeyBindInfo(boolean hasBuildModeToggleKey, boolean hasBuildModeCycleKey) {
    public static final StreamCodec<ByteBuf, KeyBindInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            KeyBindInfo::hasBuildModeToggleKey,
            ByteBufCodecs.BOOL,
            KeyBindInfo::hasBuildModeCycleKey,
            KeyBindInfo::new);
}
