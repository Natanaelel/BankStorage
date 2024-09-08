package net.natte.bankstorage.packet.server;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

public record ToggleBuildModePacketC2S() implements CustomPacketPayload {

    public static final ToggleBuildModePacketC2S INSTANCE = new ToggleBuildModePacketC2S();

    public static final Type<ToggleBuildModePacketC2S> TYPE = new Type<>(Util.ID("toggle_buildmode_c2s"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleBuildModePacketC2S> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<ToggleBuildModePacketC2S> type() {
        return TYPE;
    }

    public static void handle(ToggleBuildModePacketC2S packet, IPayloadContext context) {
        ItemStack bankItem = getBankLike(context.player());
        if (bankItem == null)
            return;

        bankItem.update(BankStorage.OptionsComponentType, BankOptions.DEFAULT, BankOptions::nextBuildMode);

        context.player().displayClientMessage(Component.translatable("popup.bankstorage.buildmode."
                + bankItem.get(BankStorage.OptionsComponentType).buildMode().toString().toLowerCase()), true);
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
