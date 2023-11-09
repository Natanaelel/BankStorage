package net.natte.bankstorage.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.natte.bankstorage.options.BankOptions;

public class ServerEvents {

    public static void onToggleBuildMode(ServerPlayerEntity player) {
        if (Util.isBankLike(player.getMainHandStack())) {

            BankOptions options = Util.getOrCreateOptions(player.getMainHandStack());
            options.buildMode = options.buildMode.next();
            Util.setOptions(player.getMainHandStack(), options);
            player.sendMessage(Text.translatable("popup.bankstorage.buildmode."
                    + options.buildMode.toString().toLowerCase()), true);
        } else if (Util.isBankLike(player.getOffHandStack())) {

            BankOptions options = Util.getOrCreateOptions(player.getOffHandStack());
            options.buildMode = options.buildMode.next();
            Util.setOptions(player.getOffHandStack(), options);
            player.sendMessage(Text.translatable("popup.bankstorage.buildmode."
                    + options.buildMode.toString().toLowerCase()), true);
        }
    }
}
