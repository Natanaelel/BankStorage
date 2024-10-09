package net.natte.bankstorage.mixin;

import java.util.Random;

import net.natte.bankstorage.access.KeyBindInfoAccess;
import net.natte.bankstorage.util.KeyBindInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.entity.player.PlayerEntity;
import net.natte.bankstorage.access.SyncedRandomAccess;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements SyncedRandomAccess, KeyBindInfoAccess {
    @Unique
    private Random random;

    @Override
    public Random bankstorage$getSyncedRandom() {
        return this.random;
    }

    @Override
    public void bankstorage$setSyncedRandom(Random random) {
        this.random = random;
    }


    @Unique
    private KeyBindInfo keyBindInfo;

    @Override
    public KeyBindInfo bankstorge$getKeyBindInfo() {
        return this.keyBindInfo;
    }

    @Override
    public void bankstorge$setKeyBindInfo(KeyBindInfo keyBindInfo) {
        this.keyBindInfo = keyBindInfo;
    }
}
