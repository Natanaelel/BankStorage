package net.natte.bankstorage.options;

import net.minecraft.nbt.NbtCompound;

public class BankOptions {
    public PickupMode pickupMode = PickupMode.NONE;
    public BuildMode buildMode = BuildMode.NONE;

    public BankOptions() {

    }


    public NbtCompound asNbt() {
        NbtCompound nbt = new NbtCompound();

        nbt.putByte("pickup", pickupMode.number);
        nbt.putByte("build", buildMode.number);

        return nbt;
    }

    public static BankOptions fromNbt(NbtCompound nbt) {
        BankOptions options = new BankOptions();

        options.pickupMode = PickupMode.from(nbt.getByte("pickup"));
        options.buildMode = BuildMode.from(nbt.getByte("build"));

        return options;
    }
}