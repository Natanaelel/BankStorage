package net.natte.bankstorage.world;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.item.BankItem;

public class BankStateSaverAndLoader extends PersistentState {
    public Map<UUID, BankItemStorage> bankMap = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbtCompound) {

        BankStorage.LOGGER.info("Saving banks to nbt");

        NbtList nbtList = new NbtList();

        bankMap.forEach((uuid, bankItemStorage) -> {
            NbtCompound nbt = new NbtCompound();
            nbt.putUuid(BankItem.UUID_KEY, uuid);
            nbt.put("bank", bankItemStorage.saveToNbt());
            nbtList.add(nbt);
        });

        nbtCompound.put("BankStates", nbtList);

        BankStorage.LOGGER.info("Saving done");

        return nbtCompound;
    }

    public static BankStateSaverAndLoader createFromNbt(NbtCompound tag) {

        BankStorage.LOGGER.info("Loading banks from nbt");

        BankStateSaverAndLoader state = new BankStateSaverAndLoader();

        state.bankMap.clear();

        NbtList nbtList = tag.getList("BankStates", NbtElement.COMPOUND_TYPE);

        for (NbtElement nbtElement : nbtList) {
            NbtCompound nbt = (NbtCompound) nbtElement;
            UUID uuid = nbt.getUuid(BankItem.UUID_KEY);
            BankItemStorage bankItemStorage = BankItemStorage.createFromNbt(nbt.getCompound("bank"));
            state.bankMap.put(uuid, bankItemStorage);
        }

        BankStorage.LOGGER.info("Loading done");

        return state;
    }

    public static BankStateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        BankStateSaverAndLoader state = persistentStateManager.getOrCreate(
                BankStateSaverAndLoader::createFromNbt,
                BankStateSaverAndLoader::new,
                BankStorage.MOD_ID);

        state.markDirty();

        return state;
    }

    public BankItemStorage getOrCreate(UUID uuid, BankType type, Text name) {
        if (this.bankMap.containsKey(uuid)) {
            BankItemStorage old = this.bankMap.get(uuid).withDisplayName(name);
            if(old.type != type){
                BankItemStorage ugpraded = old.asType(type);
                
                this.bankMap.put(uuid, ugpraded);
                return ugpraded;
            }
            return old;
            
        } else {
            BankStorage.LOGGER.info("creating new bank with uuid " + uuid);
            BankItemStorage bankItemStorage = new BankItemStorage(type, uuid);
            this.bankMap.put(uuid, bankItemStorage);
            return bankItemStorage.withDisplayName(name);
        }
    }

    public BankItemStorage get(UUID uuid) {
        return this.bankMap.get(uuid);
    }
}
