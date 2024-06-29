package net.natte.bankstorage.command;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.command.BankTypeArgumentType.BankType;
import net.natte.bankstorage.container.BankItemStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class BankFilter {

    private final List<BankType> bankTypeFilter;
    private final List<UUID> playerFilter;
    private final List<Predicate<ItemStack>> itemFilter;

    public BankFilter() {
        bankTypeFilter = new ArrayList<>();
        playerFilter = new ArrayList<>();
        itemFilter = new ArrayList<>();
    }

    public void clear() {
        clearType();
        clearPlayers();
        clearItems();
    }

    public void clearType() {
        bankTypeFilter.clear();
    }

    public void clearPlayers() {
        playerFilter.clear();
    }

    public void clearItems() {
        itemFilter.clear();
    }

    public void addPlayer(ServerPlayer player) {
        playerFilter.add(player.getUUID());
    }

    public void addType(BankType bankType) {
        bankTypeFilter.add(bankType);
    }

    public void addItemPredicate(Predicate<ItemStack> itemStackPredicate) {
        itemFilter.add(itemStackPredicate);
    }

    public boolean matchesBank(BankItemStorage bankItemStorage) {
        return matchesType(bankItemStorage)
                && matchesPlayer(bankItemStorage)
                && matchesItems(bankItemStorage);
    }

    private boolean matchesType(BankItemStorage bankItemStorage) {
        if (bankTypeFilter.isEmpty())
            return true;

        return bankTypeFilter.contains(getBankType(bankItemStorage));
    }

    private BankType getBankType(BankItemStorage bankItemStorage) {
        return switch (bankItemStorage.type().getName()) {
            case "bank_1" -> BankType.BANK_1;
            case "bank_2" -> BankType.BANK_2;
            case "bank_3" -> BankType.BANK_3;
            case "bank_4" -> BankType.BANK_4;
            case "bank_5" -> BankType.BANK_5;
            case "bank_6" -> BankType.BANK_6;
            case "bank_7" -> BankType.BANK_7;
            default -> null; // impossible
        };
    }

    private boolean matchesPlayer(BankItemStorage bankItemStorage) {
        if (playerFilter.isEmpty())
            return true;
        return playerFilter.contains(bankItemStorage.usedByPlayerUUID);
    }

    private boolean matchesItems(BankItemStorage bankItemStorage) {
        if (itemFilter.isEmpty())
            return true;

        for (Predicate<ItemStack> predicate : itemFilter) {
            boolean hasMatchingStack = bankItemStorage.getItems().stream().anyMatch(predicate);
            if (!hasMatchingStack)
                return false;
        }
        return true;
    }
}
