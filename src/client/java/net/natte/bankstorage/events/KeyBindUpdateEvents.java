package net.natte.bankstorage.events;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.natte.bankstorage.BankStorageClient;
import net.natte.bankstorage.packet.server.KeyBindUpdatePacketC2S;
import net.natte.bankstorage.util.KeyBindInfo;
import net.natte.bankstorage.util.Util;

public class KeyBindUpdateEvents {
    public static void onKeyBindChange() {
        boolean isToggleKeyUnbound = BankStorageClient.toggleBuildModeKeyBinding.isUnbound();
        boolean isCycleKeyUnbound = BankStorageClient.cycleBuildModeKeyBinding.isUnbound();
        KeyBindInfo keyBindInfo = new KeyBindInfo(!isToggleKeyUnbound, !isCycleKeyUnbound);
        Util.keyBindInfo = keyBindInfo;
        if (ClientPlayNetworking.canSend(KeyBindUpdatePacketC2S.TYPE))
            ClientPlayNetworking.send(new KeyBindUpdatePacketC2S(keyBindInfo));
    }
}
