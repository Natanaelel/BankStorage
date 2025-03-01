package net.natte.bankstorage.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.Random;

public record BankOptions(PickupMode pickupMode, BuildMode buildMode, SortMode sortMode, short uniqueId) {

    public static final BankOptions DEFAULT = new BankOptions(PickupMode.NONE, BuildMode.NONE_NORMAL, SortMode.COUNT, (short) 0);

    public static final StreamCodec<ByteBuf, BankOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE,
            o -> (byte) o.pickupMode.ordinal(),
            ByteBufCodecs.BYTE,
            o -> (byte) o.buildMode.ordinal(),
            ByteBufCodecs.BYTE,
            o -> (byte) o.sortMode.ordinal(),
            ByteBufCodecs.SHORT,
            BankOptions::uniqueId,
            BankOptions::of);

    private static final Random random = new Random();

    public static final Codec<BankOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BYTE.fieldOf("pickup").forGetter(o -> (byte) o.pickupMode.ordinal()),
            Codec.BYTE.fieldOf("build").forGetter(o -> (byte) o.buildMode.ordinal()),
            Codec.BYTE.fieldOf("sort").forGetter(o -> (byte) o.sortMode.ordinal()),
            Codec.SHORT.fieldOf("id").orElseGet(() -> (short) random.nextInt()).forGetter(o -> o.uniqueId)
    ).apply(instance, BankOptions::of));


    public static BankOptions of(byte pickupMode, byte buildMode, byte sortMode, short uniqueId) {

        return new BankOptions(
                PickupMode.values()[pickupMode],
                BuildMode.values()[buildMode],
                SortMode.values()[sortMode],
                uniqueId);
    }

    public static BankOptions create(){
        return new BankOptions(PickupMode.NONE, BuildMode.NONE_NORMAL, SortMode.COUNT, (short) random.nextInt());
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

    public BankOptions withBuildMode(BuildMode newBuildMode) {
        return new BankOptions(pickupMode, newBuildMode, sortMode, uniqueId);
    }

    public BankOptions nextBuildMode() {
        return withBuildMode(buildMode.next());
    }

    public BankOptions withSortMode(SortMode newSortMode) {
        return new BankOptions(pickupMode, buildMode, newSortMode, uniqueId);
    }

    public BankOptions nextPickupMode() {
        return new BankOptions(pickupMode.next(), buildMode, sortMode, uniqueId);
    }
}
