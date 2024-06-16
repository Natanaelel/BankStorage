package net.natte.bankstorage.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

import java.util.Objects;

// TODO: make immutable...
public class BankOptions {
    public PickupMode pickupMode = PickupMode.NONE;
    public BuildMode buildMode = BuildMode.NONE;
    public SortMode sortMode = SortMode.COUNT;

    public int selectedItemSlot = 0;

    public static final StreamCodec<ByteBuf, BankOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE,
            o -> (byte) o.pickupMode.ordinal(),
            ByteBufCodecs.BYTE,
            o -> (byte) o.buildMode.ordinal(),
            ByteBufCodecs.BYTE,
            o -> (byte) o.sortMode.ordinal(),
            ByteBufCodecs.INT,
            o -> o.selectedItemSlot,
            BankOptions::of);

    public static final Codec<BankOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BYTE.fieldOf("pickup").forGetter(o -> (byte) o.pickupMode.ordinal()),
            Codec.BYTE.fieldOf("build").forGetter(o -> (byte) o.buildMode.ordinal()),
            Codec.BYTE.fieldOf("sort").forGetter(o -> (byte) o.sortMode.ordinal()),
            Codec.INT.fieldOf("slot").forGetter(o -> o.selectedItemSlot)).apply(instance, BankOptions::of));

    public static BankOptions of(byte pickupMode, byte buildMode, byte sortMode, int selectedItemSlot) {
        BankOptions options = new BankOptions();

        options.pickupMode = PickupMode.values()[pickupMode];
        options.buildMode = BuildMode.values()[buildMode];
        options.sortMode = SortMode.values()[sortMode];
        options.selectedItemSlot = selectedItemSlot;
        return options;
    }

    public BankOptions copy() {
        BankOptions copy = new BankOptions();
        copy.pickupMode = pickupMode;
        copy.buildMode = buildMode;
        copy.sortMode = sortMode;
        copy.selectedItemSlot = selectedItemSlot;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BankOptions options = (BankOptions) o;
        return selectedItemSlot == options.selectedItemSlot && pickupMode == options.pickupMode && buildMode == options.buildMode && sortMode == options.sortMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pickupMode, buildMode, sortMode, selectedItemSlot);
    }
}
