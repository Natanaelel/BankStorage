package net.natte.bankstorage.events;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.natte.bankstorage.BankStorageClient;
import net.natte.bankstorage.packet.server.KeyBindUpdatePacketC2S;
import net.natte.bankstorage.util.Util;

public class KeyBindUpdateEvents {
    public static void onKeyBindChange() {
        boolean isUnbound = BankStorageClient.toggleBuildModeKeyBinding.isUnbound();
        Util.isBuildModeKeyUnBound = isUnbound;
        if (ClientPlayNetworking.canSend(KeyBindUpdatePacketC2S.PACKET_ID))
            ClientPlayNetworking.send(new KeyBindUpdatePacketC2S(isUnbound));
    }
}
