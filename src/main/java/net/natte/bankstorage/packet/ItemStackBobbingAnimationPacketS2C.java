package net.natte.bankstorage.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPacketHandler;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;

public class ItemStackBobbingAnimationPacketS2C
        implements FabricPacket {

    public static final PacketType<ItemStackBobbingAnimationPacketS2C> TYPE = PacketType
            .create(new Identifier(BankStorage.MOD_ID, "bobbing"), ItemStackBobbingAnimationPacketS2C::new);

    public static class Receiver implements PlayPacketHandler<ItemStackBobbingAnimationPacketS2C> {

        public void receive(ItemStackBobbingAnimationPacketS2C packet, ClientPlayerEntity player,
                PacketSender responseSender) {
            PlayerInventory inventory = player.getInventory();
            ItemStack stack = ItemStack.EMPTY;
            int i = packet.index;
            if (i < 9 * 4) {
                stack = inventory.main.get(i);
            } else if (i < 9 * 4 + 1) {
                stack = inventory.offHand.get(i - 9 * 4);
            } else {
                stack = inventory.armor.get(i - 9 * 4 - 1);
            }
            stack.setBobbingAnimationTime(5);

        }
    }

    public int index;

    public ItemStackBobbingAnimationPacketS2C(int index) {
        this.index = index;
    }

    public ItemStackBobbingAnimationPacketS2C(PacketByteBuf buf) {
        this(buf.readInt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(this.index);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}