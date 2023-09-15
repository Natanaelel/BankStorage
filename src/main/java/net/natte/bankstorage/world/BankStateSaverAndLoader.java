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

public class BankStateSaverAndLoader extends PersistentState {
    public Map<UUID, BankItemStorage> bankMap = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbtCompound) {
        System.out.println("writeNBT");
        NbtList nbtList = new NbtList();
        
        bankMap.forEach((uuid, bankItemStorage) -> {
            NbtCompound nbt = new NbtCompound();
            nbt.putUuid("uuid", uuid);
            nbt.put("bank", bankItemStorage.saveToNbt());
            nbtList.add(nbt);
        });
        
        nbtCompound.put("BankStates", nbtList);
        System.out.println("writeNBT done");

        return nbtCompound;
    }

    public static BankStateSaverAndLoader createFromNbt(NbtCompound tag) {
        
        BankStateSaverAndLoader state = new BankStateSaverAndLoader();

        state.bankMap.clear();
        
        NbtList nbtList = tag.getList("BankStates", NbtElement.COMPOUND_TYPE);

        for(NbtElement nbtElement : nbtList){
            NbtCompound nbt = (NbtCompound) nbtElement;
            UUID uuid = nbt.getUuid("uuid");
            BankItemStorage bankItemStorage = BankItemStorage.createFromNbt(nbt.getCompound("bank"));
            state.bankMap.put(uuid, bankItemStorage);
        }

        return state;
    }

    public static BankStateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
 
        BankStateSaverAndLoader state = persistentStateManager.getOrCreate(
                BankStateSaverAndLoader::createFromNbt,
                BankStateSaverAndLoader::new,
                BankStorage.MOD_ID
        );
 
        // If state is not marked dirty, when Minecraft closes, 'writeNbt' won't be called and therefore nothing will be saved.
        // Technically it's 'cleaner' if you only mark state as dirty when there was actually a change, but the vast majority
        // of mod writers are just going to be confused when their data isn't being saved, and so it's best just to 'markDirty' for them.
        // Besides, it's literally just setting a bool to true, and the only time there's a 'cost' is when the file is written to disk when
        // there were no actual change to any of the mods state (INCREDIBLY RARE).
        state.markDirty();
 
        return state;
    }

    public BankItemStorage getOrCreate(UUID uuid, BankType type, Text name){
        if(this.bankMap.containsKey(uuid)){
            return this.bankMap.get(uuid).withDisplayName(name);
        }else{
            BankItemStorage bankItemStorage = new BankItemStorage(type);
            this.bankMap.put(uuid, bankItemStorage);
            return bankItemStorage.withDisplayName(name);
        }
    }
}
