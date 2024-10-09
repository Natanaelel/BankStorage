package net.natte.bankstorage.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.natte.bankstorage.client.BankStorageClient;
import net.natte.bankstorage.packet.server.KeyBindUpdatePacketC2S;
import net.natte.bankstorage.util.KeyBindInfo;
import net.natte.bankstorage.util.Util;

public class KeyBindUpdateEvents {
    public static void onKeyBindChange() {
        boolean isToggleBuildModeUnbound = BankStorageClient.toggleBuildModeKeyBinding.isUnbound();
        boolean isCycleBuildModeUnbound = BankStorageClient.cycleBuildModeKeyBinding.isUnbound();
        KeyBindInfo keybindInfo = new KeyBindInfo(!isToggleBuildModeUnbound, !isCycleBuildModeUnbound);
        Util.keybindInfo = keybindInfo;
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null)
            return;
        if (connection.hasChannel(KeyBindUpdatePacketC2S.TYPE))
            connection.send(new KeyBindUpdatePacketC2S(keybindInfo));
    }
}
