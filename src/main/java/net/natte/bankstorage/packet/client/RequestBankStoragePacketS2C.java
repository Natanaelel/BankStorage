package net.natte.bankstorage.packet.client;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.util.Util;

public record RequestBankStoragePacketS2C(CachedBankStorage cachedBankStorage) implements CustomPayload {

    public static final CustomPayload.Id<RequestBankStoragePacketS2C> PACKET_ID = new CustomPayload.Id<>(Util.ID("requestbank_s2c"));
    public static final PacketCodec<RegistryByteBuf, RequestBankStoragePacketS2C> PACKET_CODEC = CachedBankStorage.PACKET_CODEC
            .xmap(
                    RequestBankStoragePacketS2C::new,
                    RequestBankStoragePacketS2C::cachedBankStorage);

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
