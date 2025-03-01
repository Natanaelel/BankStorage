package net.natte.bankstorage.packet.server;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

public record CycleBuildModePacketC2S() implements CustomPacketPayload {

    public static final CycleBuildModePacketC2S INSTANCE = new CycleBuildModePacketC2S();

    public static final Type<CycleBuildModePacketC2S> TYPE = new Type<>(Util.ID("cycle_buildmode_c2s"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CycleBuildModePacketC2S> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<CycleBuildModePacketC2S> type() {
        return TYPE;
    }

    public static void handle(CycleBuildModePacketC2S packet, IPayloadContext context) {
        ItemStack bankItem = getBankLike(context.player());
        if (bankItem == null)
            return;

        BankOptions options = Util.getOrCreateOptions(bankItem);

        if (!options.buildMode().isActive())
            return;

        BuildMode newBuildMode = options.buildMode().cycle();

        bankItem.set(BankStorage.OptionsComponentType, options.withBuildMode(newBuildMode));

        context.player().displayClientMessage(Component.translatable("popup.bankstorage.buildmode."
                + newBuildMode.toString().toLowerCase()), true);
    }

    @Nullable
    private static ItemStack getBankLike(Player player) {
        if (Util.isBankLike(player.getMainHandItem()))
            return player.getMainHandItem();
        if (Util.isBankLike(player.getOffhandItem()))
            return player.getOffhandItem();
        return null;
    }
}
