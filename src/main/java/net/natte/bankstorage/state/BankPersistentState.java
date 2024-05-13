package net.natte.bankstorage.state;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.PersistentState;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;

public class BankPersistentState extends PersistentState {

    private static final String BANK_DATA_KEY = "bank_data";
    private final Map<UUID, BankItemStorage> BANK_MAP;

    public BankPersistentState() {
        BANK_MAP = new HashMap<>();
    }

    public static BankPersistentState createFromNbt(NbtCompound nbtCompound, WrapperLookup registryLookup) {

        BankPersistentState state = new BankPersistentState();

        state.BANK_MAP.clear();

        BankStorage.LOGGER.debug("Loading banks from nbt");

        List<BankItemStorage> banks = BankSerializer.CODEC
                .parse(registryLookup.getOps(NbtOps.INSTANCE), nbtCompound.get(BANK_DATA_KEY))
                .getOrThrow();

        for (BankItemStorage bank : banks) {
            state.BANK_MAP.put(bank.uuid, bank);
        }

        BankStorage.LOGGER.debug("Loading done");

        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbtCompound, WrapperLookup registryLookup) {

        BankStorage.LOGGER.debug("Saving banks to nbt");

        NbtElement bankNbt = BankSerializer.CODEC
                .encodeStart(registryLookup.getOps(NbtOps.INSTANCE), getBankItemStorages())
                .resultOrPartial(BankStorage.LOGGER::error)
                .orElse(new NbtCompound());

        nbtCompound.put(BANK_DATA_KEY, bankNbt);
        BankStorage.LOGGER.debug("Saving done");

        return nbtCompound;
    }

    @Nullable
    public BankItemStorage get(UUID uuid) {
        return this.BANK_MAP.get(uuid);
    }

    @Nullable
    public BankItemStorage getOrCreate(UUID uuid, BankType type) {
        BankItemStorage bank = this.BANK_MAP.get(uuid);
        if (bank == null) {
            bank = new BankItemStorage(type, uuid);
            set(uuid, bank);
        }
        return bank;
    }

    public void set(UUID uuid, BankItemStorage bankItemStorage) {
        this.BANK_MAP.put(uuid, bankItemStorage);
    }

    public List<BankItemStorage> getBankItemStorages() {
        return List.copyOf(BANK_MAP.values());
    }

}
