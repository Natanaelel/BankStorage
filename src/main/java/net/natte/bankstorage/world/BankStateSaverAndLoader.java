package net.natte.bankstorage.world;

import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;

public class BankStateSaverAndLoader extends PersistentState {

    private GlobalBankState state;

    public BankStateSaverAndLoader() {
        this.state = new GlobalBankState();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {

        BankStorage.LOGGER.info("Saving banks to nbt");

        VersionStateSaverAndLoader.writeNbt(this.state, nbtCompound);
        BankStorage.LOGGER.info("Saving done");

        return nbtCompound;
    }

    public static BankStateSaverAndLoader createFromNbt(NbtCompound nbtCompound, WrapperLookup registryLookup) {

        BankStorage.LOGGER.info("Loading banks from nbt");

        BankStateSaverAndLoader stateSaverAndLoader = VersionStateSaverAndLoader.readNbt(nbtCompound, registryLookup);

        BankStorage.LOGGER.info("Loading done");

        return stateSaverAndLoader;
    }

    public static BankStateSaverAndLoader getServerStateSaverAndLoader(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        // ServerWorld world = (ServerWorld) (Object) this;
		PersistentState.Type<BankStateSaverAndLoader> type = new PersistentState.Type<>(
				() -> new BankStateSaverAndLoader(),
				(nbt, registryLookup) -> createFromNbt(nbt, registryLookup),
				null // Object builder API 12.1.0 and later makes this a no-op
		);
        // BankStateSaverAndLoader state = persistentStateManager.getOrCreate(
        //         BankStateSaverAndLoader::createFromNbt,
        //         BankStateSaverAndLoader::new,
        //         BankStorage.MOD_ID);
        BankStateSaverAndLoader state = persistentStateManager.getOrCreate(
                type,
                BankStorage.MOD_ID);
        

        state.markDirty();

        return state;
    }

    public BankItemStorage getOrCreate(UUID uuid, BankType type) {
        if (this.state.getBankMap().containsKey(uuid)) {
            BankItemStorage old = this.state.getBankMap().get(uuid);
            if (old.type != type) {
                BankItemStorage upgraded = old.asType(type);

                this.state.getBankMap().put(uuid, upgraded);
                return upgraded;
            }
            return old;

        } else {
            BankStorage.LOGGER.info("creating new bank with uuid " + uuid);
            BankItemStorage bankItemStorage = new BankItemStorage(type, uuid);
            bankItemStorage.dateCreated = LocalDateTime.now();
            this.state.getBankMap().put(uuid, bankItemStorage);
            return bankItemStorage;
        }
    }

    public BankItemStorage get(UUID uuid) {
        return this.state.get(uuid);
    }

    public Map<UUID, BankItemStorage> getBankMap() {
        return this.state.getBankMap();
    }

    public GlobalBankState getState() {
        return this.state;
    }
}
