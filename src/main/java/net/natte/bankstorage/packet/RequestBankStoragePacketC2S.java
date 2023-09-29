package net.natte.bankstorage.packet;

import java.util.List;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.util.Util;

public class RequestBankStoragePacketC2S implements FabricPacket {

    public static final PacketType<RequestBankStoragePacketC2S> TYPE = PacketType
            .create(new Identifier(BankStorage.MOD_ID, "requestbank_c2s"), RequestBankStoragePacketC2S::new);

    public static class Receiver implements
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler<RequestBankStoragePacketC2S> {

        @Override
        public void receive(RequestBankStoragePacketC2S packet, ServerPlayerEntity player,
                PacketSender responseSender) {

            BankItemStorage bankItemStorage = Util.getBankItemStorage(packet.uuid, player.getWorld());
            long randomSeed = (long) (Math.random() * 0xBEEEF);
            bankItemStorage.random.setSeed(randomSeed);
            List<ItemStack> items = bankItemStorage.getBlockItems();
            bankItemStorage.options.selectedItemSlot = Math
                    .max(Math.min(bankItemStorage.options.selectedItemSlot, items.size() - 1), 0);
            responseSender.sendPacket(new RequestBankStoragePacketS2C(
                    new CachedBankStorage(items, packet.uuid, bankItemStorage.options, randomSeed), randomSeed));
        }
    }

    public UUID uuid;

    public RequestBankStoragePacketC2S(UUID uuid) {
        this.uuid = uuid;
    }

    public RequestBankStoragePacketC2S(PacketByteBuf buf) {
        this(buf.readUuid());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.uuid);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
