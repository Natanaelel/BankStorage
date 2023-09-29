package net.natte.bankstorage.options;

import net.minecraft.nbt.NbtCompound;

public class BankOptions {
    public PickupMode pickupMode = PickupMode.NONE;
    public BuildMode buildMode = BuildMode.NONE;

    public int selectedItemSlot = 0;

    public NbtCompound asNbt() {
        return writeNbt(new NbtCompound());
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putByte("pickup", pickupMode.number);
        nbt.putByte("build", buildMode.number);
        nbt.putInt("slot", selectedItemSlot);

        return nbt;
    }

    public static BankOptions fromNbt(NbtCompound nbt) {
        BankOptions options = new BankOptions();

        options.pickupMode = PickupMode.from(nbt.getByte("pickup"));
        options.buildMode = BuildMode.from(nbt.getByte("build"));
        options.selectedItemSlot = nbt.getInt("slot");

        return options;
    }
}
