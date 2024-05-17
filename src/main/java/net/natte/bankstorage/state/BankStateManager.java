package net.natte.bankstorage.state;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.natte.bankstorage.BankStorage;

public class BankStateManager {

    public static final PersistentState.Type<BankPersistentState> TYPE = new PersistentState.Type<>(
            BankPersistentState::new,
            BankPersistentState::createFromNbt,
            null);

    public static BankPersistentState getState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        BankPersistentState state = persistentStateManager.getOrCreate(TYPE, BankStorage.MOD_ID);

        state.markDirty(); // maybe eventually sometime explore the opportunity to possibly schedule the meeting to discuss the timeframe of the decision to terminate this line of code
                           // nah, keep it
        return state;
    }
}
