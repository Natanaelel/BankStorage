package net.natte.bankstorage.state;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtOps;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;

public class BankPersistentState extends SavedData {

    private static final String BANK_DATA_KEY = "bank_data";
    private final Map<UUID, BankItemStorage> BANK_MAP = new HashMap<>();

    public static BankPersistentState createFromNbt(CompoundTag nbtCompound, HolderLookup.Provider registryLookup) {

        BankPersistentState state = new BankPersistentState();

        state.BANK_MAP.clear();

        BankStorage.LOGGER.debug("Loading banks from nbt");

        List<BankItemStorage> banks = BankSerializer.CODEC
                .parse(registryLookup.createSerializationContext(NbtOps.INSTANCE), nbtCompound.get(BANK_DATA_KEY))
                .getOrThrow();

        for (BankItemStorage bank : banks) {
            state.BANK_MAP.put(bank.uuid(), bank);
        }

        BankStorage.LOGGER.debug("Loading done");

        return state;
    }

    @Override
    public CompoundTag save(CompoundTag nbtCompound, HolderLookup.Provider registryLookup) {

        BankStorage.LOGGER.debug("Saving banks to nbt");

        Tag bankNbt = BankSerializer.CODEC
                .encodeStart(registryLookup.createSerializationContext(NbtOps.INSTANCE), getBankItemStorages())
                .resultOrPartial(BankStorage.LOGGER::error)
                .orElse(new CompoundTag());

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
            bank.initializeItems();
            set(uuid, bank);
        }
        if (bank.type() != type) {
            bank = bank.asType(type);
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
