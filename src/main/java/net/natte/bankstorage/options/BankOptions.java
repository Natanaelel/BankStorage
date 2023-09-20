package net.natte.bankstorage.options;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public class BankOptions {
    public PickupMode pickupMode = PickupMode.NONE;
    public BuildMode buildMode = BuildMode.NONE;

    public BankOptions() {

    }


    public NbtCompound asNbt() {
        return writeNbt(new NbtCompound());
    }
    public NbtCompound writeNbt(NbtCompound nbt){
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

    public static BankOptions readPacketByteBuf(PacketByteBuf buf){
        BankOptions options = new BankOptions();

        options.pickupMode = PickupMode.from(buf.readByte());
        options.buildMode = BuildMode.from(buf.readByte());

        return options;
    }

    public void writeToPacketByteBuf(PacketByteBuf buf){
        buf.writeByte(this.pickupMode.number);
        buf.writeByte(this.buildMode.number);
    }
}