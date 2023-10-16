package net.natte.bankstorage.access;

import java.util.Random;

public interface SyncedRandomAccess {
    public Random bankstorage$getSyncedRandom();
    public void bankstorage$setSyncedRandom(Random random);
}
