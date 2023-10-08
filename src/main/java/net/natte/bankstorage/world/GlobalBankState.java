package net.natte.bankstorage.world;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.natte.bankstorage.container.BankItemStorage;

public class GlobalBankState {
    private final Map<UUID, BankItemStorage> BANK_MAP;

    public GlobalBankState(){
        this.BANK_MAP = new HashMap<>();
    }

    public void clear(){
        this.BANK_MAP.clear();
    }

    public Map<UUID, BankItemStorage> getBankMap(){
        return this.BANK_MAP;
    }

    public void put(UUID uuid, BankItemStorage bankItemStorage){
        this.BANK_MAP.put(uuid, bankItemStorage);
    }

    public @Nullable BankItemStorage get(UUID uuid){
        return this.BANK_MAP.get(uuid);
    }
}
