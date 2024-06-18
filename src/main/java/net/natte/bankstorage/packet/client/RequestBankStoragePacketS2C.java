package net.natte.bankstorage.packet.client;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestBankStoragePacketS2C(CachedBankStorage cachedBankStorage) implements CustomPacketPayload {

    public static final Type<RequestBankStoragePacketS2C> TYPE = new Type<>(Util.ID("requestbank_s2c"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestBankStoragePacketS2C> STREAM_CODEC = CachedBankStorage.STREAM_CODEC
            .map(
                    RequestBankStoragePacketS2C::new,
                    RequestBankStoragePacketS2C::cachedBankStorage);

    @Override
    public Type<RequestBankStoragePacketS2C> type() {
        return TYPE;
    }

    public static void handle(RequestBankStoragePacketS2C packet, IPayloadContext context) {

        CachedBankStorage bankStorage = packet.cachedBankStorage();
        CachedBankStorage.setBankStorage(bankStorage.uuid, bankStorage);

//        if (bankStorage.uuid.equals(BankStorageClient.buildModePreviewRenderer.uuid)) {
//            BankStorageClient.buildModePreviewRenderer.setBankStorage(bankStorage);

//            int selectedSlot = BankStorageClient.buildModePreviewRenderer.optimisticOptions.selectedSlot();
//            int newSelectedSlot = Mth.clamp(selectedSlot, 0, bankStorage.blockItems.size() - 1);
//            if (newSelectedSlot != selectedSlot) {
//                BankOptions options = BankStorageClient.buildModePreviewRenderer.options;
//                options.selectedItemSlot = newSelectedSlot;
//                context.listener().send(new UpdateBankOptionsPacketC2S(options));
//            }
//        }
    }
}
