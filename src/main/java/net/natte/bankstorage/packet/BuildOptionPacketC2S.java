
package net.natte.bankstorage.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.util.Util;

public class BuildOptionPacketC2S implements FabricPacket {

    public static final PacketType<BuildOptionPacketC2S> TYPE = PacketType
            .create(new Identifier(BankStorage.MOD_ID, "build_option_c2s"), BuildOptionPacketC2S::new);

    public static class Receiver implements PlayPacketHandler<BuildOptionPacketC2S> {

        @Override
        public void receive(BuildOptionPacketC2S packet, ServerPlayerEntity player, PacketSender responseSender) {
            // cycle build mode
            ItemStack stackInHand = player.getStackInHand(player.getActiveHand());
            if (Util.isBank(stackInHand)) {
                BankItemStorage bankItemStorage = Util.getBankItemStorage(stackInHand,
                        player.getWorld());
                bankItemStorage.options.buildMode = BuildMode
                        .from((bankItemStorage.options.buildMode.number + 1) % 3);
                player.sendMessage(Text.translatable("popup.bankstorage.buildmode."
                        + bankItemStorage.options.buildMode.toString().toLowerCase()), true);

                responseSender
                        .sendPacket(new OptionPacketS2C(Util.getUUID(stackInHand), bankItemStorage.options.asNbt()));
            }
        }
    }

    public BuildOptionPacketC2S() {
    }

    public BuildOptionPacketC2S(PacketByteBuf buf) {
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}