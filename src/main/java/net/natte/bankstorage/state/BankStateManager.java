package net.natte.bankstorage.state;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.natte.bankstorage.BankStorage;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public class BankStateManager {

    public static final SavedData.Factory<BankPersistentState> TYPE = new SavedData.Factory<>(
            BankPersistentState::new,
            BankPersistentState::createFromNbt,
            null);

    private static BankPersistentState INSTANCE;

    public static void initialize(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        DimensionDataStorage persistentStateManager = server.overworld().getDataStorage();

        INSTANCE = persistentStateManager.computeIfAbsent(TYPE, BankStorage.MOD_ID);
    }

    // get state and setDirty
    public static BankPersistentState getState() {
        INSTANCE.setDirty(); // could be optimized?
        return INSTANCE;
    }

    // this maybe makes the above setDirty unnecessary
    public static void markDirty() {
        getState().setDirty();
    }
}
