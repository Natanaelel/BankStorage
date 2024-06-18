package net.natte.bankstorage.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

// revision increments only on serverside when an item's options should replace the client's BuildModePreviewRenderer.optimisticOptions, otherwise client options take priority
public record BankOptions(PickupMode pickupMode, BuildMode buildMode, SortMode sortMode) {

    public static final BankOptions DEFAULT = new BankOptions(PickupMode.NONE, BuildMode.NONE, SortMode.COUNT);

    public static final StreamCodec<ByteBuf, BankOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE,
            o -> (byte) o.pickupMode.ordinal(),
            ByteBufCodecs.BYTE,
            o -> (byte) o.buildMode.ordinal(),
            ByteBufCodecs.BYTE,
            o -> (byte) o.sortMode.ordinal(),
            BankOptions::of);

    public static final Codec<BankOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BYTE.fieldOf("pickup").forGetter(o -> (byte) o.pickupMode.ordinal()),
            Codec.BYTE.fieldOf("build").forGetter(o -> (byte) o.buildMode.ordinal()),
            Codec.BYTE.fieldOf("sort").forGetter(o -> (byte) o.sortMode.ordinal())
    ).apply(instance, BankOptions::of));


    public static BankOptions of(byte pickupMode, byte buildMode, byte sortMode) {
        return new BankOptions(
                PickupMode.values()[pickupMode],
                BuildMode.values()[buildMode],
                SortMode.values()[sortMode]);
    }

    @Deprecated(forRemoval = true)
    public BankOptions copy() {
        return this;
//        BankOptions copy = new BankOptions();
//        copy.pickupMode = pickupMode;
//        copy.buildMode = buildMode;
//        copy.sortMode = sortMode;
//        copy.selectedItemSlot = selectedItemSlot;
//        return copy;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        BankOptions options = (BankOptions) other;
        return pickupMode == options.pickupMode && buildMode == options.buildMode && sortMode == options.sortMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pickupMode, buildMode, sortMode);
    }

    public BankOptions nextBuildMode() {
        return new BankOptions(pickupMode, buildMode.next(), sortMode);
    }

    public BankOptions withSortMode(SortMode newSortMode) {
        return new BankOptions(pickupMode, buildMode, newSortMode);
    }

    public BankOptions nextPickupMode() {
        return new BankOptions(pickupMode.next(), buildMode, sortMode);
    }
}
