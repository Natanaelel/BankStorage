package net.natte.bankstorage.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.natte.bankstorage.client.BankStorageClient;
import net.natte.bankstorage.packet.server.KeyBindUpdatePacketC2S;
import net.natte.bankstorage.util.Util;

public class KeyBindUpdateEvents {
    public static void onKeyBindChange() {
        boolean isUnbound = BankStorageClient.toggleBuildModeKeyBinding.isUnbound();
        Util.isBuildModeKeyUnBound = isUnbound;
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null)
            return;
        if (connection.hasChannel(KeyBindUpdatePacketC2S.TYPE))
            connection.send(new KeyBindUpdatePacketC2S(isUnbound));
    }
}
