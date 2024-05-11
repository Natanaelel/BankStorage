package net.natte.bankstorage.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

public class BankOptions {
    public PickupMode pickupMode = PickupMode.NONE;
    public BuildMode buildMode = BuildMode.NONE;
    public SortMode sortMode = SortMode.COUNT;

    public int selectedItemSlot = 0;

    public static final PacketCodec<PacketByteBuf, BankOptions> PACKET_CODEC = PacketCodec
            .of((opt, buf) -> buf.writeNbt(opt.asNbt()), buf -> BankOptions.fromNbt(buf.readNbt()));
    public static final Codec<BankOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BYTE.fieldOf("pickup").forGetter(o -> o.pickupMode.number),
            Codec.BYTE.fieldOf("build").forGetter(o -> o.buildMode.number),
            Codec.BYTE.fieldOf("sort").forGetter(o -> o.sortMode.number),
            Codec.INT.fieldOf("slot").forGetter(o -> o.selectedItemSlot)).apply(instance, BankOptions::of));

    public static BankOptions of(byte pickupMode, byte buildMode, byte sortMode, int selectedItemSlot) {
        BankOptions options = new BankOptions();

        options.pickupMode = PickupMode.from(pickupMode);
        options.buildMode = BuildMode.from(buildMode);
        options.sortMode = SortMode.from(sortMode);
        options.selectedItemSlot = selectedItemSlot;
        return options;
    }

    public NbtCompound asNbt() {
        return writeNbt(new NbtCompound());
    }

    private NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putByte("pickup", pickupMode.number);
        nbt.putByte("build", buildMode.number);
        nbt.putByte("sort", sortMode.number);
        nbt.putInt("slot", selectedItemSlot);

        return nbt;
    }

    public static BankOptions fromNbt(NbtCompound nbt) {
        BankOptions options = new BankOptions();

        options.pickupMode = PickupMode.from(nbt.getByte("pickup"));
        options.buildMode = BuildMode.from(nbt.getByte("build"));
        options.sortMode = SortMode.from(nbt.getByte("sort"));
        options.selectedItemSlot = nbt.getInt("slot");

        return options;
    }
}
