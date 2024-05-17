package net.natte.bankstorage.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.entity.player.PlayerEntity;
import net.natte.bankstorage.access.SyncedRandomAccess;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements SyncedRandomAccess {
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
}
