package net.natte.bankstorage.world;

import java.util.UUID;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.item.BankItem;

public class VersionStateSaverAndLoader {

    // updates when data format changes
    public static final int LATEST_DATA_VERSION = 1;

    private static final String DATA_VERSION_KEY = "data_version";
    private static final String DATA_BANK_STATES_KEY = "bank_states";

    public static NbtCompound writeNbt(GlobalBankState state, NbtCompound nbtCompound) {

        NbtList nbtList = new NbtList();

        state.getBankMap().forEach((uuid, bankItemStorage) -> {
            NbtCompound nbt = new NbtCompound();
            nbt.putUuid("uuid", uuid);
            nbt.put("bank", bankItemStorage.saveToNbt());
            nbtList.add(nbt);
        });

        nbtCompound.putInt(DATA_VERSION_KEY, LATEST_DATA_VERSION);
        nbtCompound.put(DATA_BANK_STATES_KEY, nbtList);

        return nbtCompound;
    }

    private static int getDataVersion(NbtCompound nbtCompound) {
        return nbtCompound.getInt(DATA_VERSION_KEY);
    }

    public static BankStateSaverAndLoader readNbt(NbtCompound nbtCompound, WrapperLookup registryLookup) {

        int version = getDataVersion(nbtCompound);

        // readNbt(version, nbtCompound);
        switch (version) {
            case 0:
                return readNbtVersion0(nbtCompound);
            case 1:
                return readNbtVersion1(nbtCompound);
            default:
                BankStorage.LOGGER.error(
                        "unsupported data version: " + version + ", latest data version is " + LATEST_DATA_VERSION);
                CrashReport report = CrashReport.create(new Exception(), "Loading BankStorage data");
                report.addElement("BankStorage")
                        .add("Tried loading version", version)
                        .add("Latest version", LATEST_DATA_VERSION)
                        .add("mod id", BankStorage.MOD_ID);
                throw new CrashException(report);
        }

    }

    private static BankStateSaverAndLoader readNbtVersion0(NbtCompound nbtCompound) {

        BankStateSaverAndLoader state = new BankStateSaverAndLoader();

        state.getBankMap().clear();

        NbtList nbtList = nbtCompound.getList("BankStates", NbtElement.COMPOUND_TYPE);

        for (NbtElement nbtElement : nbtList) {
            NbtCompound nbt = (NbtCompound) nbtElement;
            UUID uuid = nbt.getUuid(BankItem.UUID_KEY);
            NbtCompound bankNbt = nbt.getCompound("bank");
            bankNbt.put("LockedSlots", new NbtList());
            BankItemStorage bankItemStorage = BankItemStorage.createFromNbt(bankNbt);
            state.getBankMap().put(uuid, bankItemStorage);
        }

        return state;
    }

    private static BankStateSaverAndLoader readNbtVersion1(NbtCompound nbtCompound) {

        BankStateSaverAndLoader state = new BankStateSaverAndLoader();

        state.getBankMap().clear();

        NbtList nbtList = nbtCompound.getList(DATA_BANK_STATES_KEY, NbtElement.COMPOUND_TYPE);

        for (NbtElement nbtElement : nbtList) {
            NbtCompound nbt = (NbtCompound) nbtElement;
            UUID uuid = nbt.getUuid("uuid");
            BankItemStorage bankItemStorage = BankItemStorage.createFromNbt(nbt.getCompound("bank"));
            state.getBankMap().put(uuid, bankItemStorage);
        }

        return state;
    }

}
