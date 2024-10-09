package net.natte.bankstorage.container;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.item.BankItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Objects;
import java.util.function.Supplier;

public class BankType {

    public static final Codec<BankType> CODEC = Codec.STRING.xmap(BankType::getBankTypeFromName, BankType::getName);
    public static final StreamCodec<ByteBuf, BankType> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(BankType::getBankTypeFromName, BankType::getName);

    private final String name;
    public final int rows;
    public final int cols;

    public final int stackLimit;
    public DeferredHolder<Item, BankItem> item;

    public BankType(String name, int stackLimit, int rows, int cols) {
        this.name = name;
        this.rows = rows;
        this.cols = cols;

        this.stackLimit = stackLimit;
    }

    public void register(DeferredRegister.Items itemRegister) {
        register(itemRegister, Item.Properties::new);
    }

    public void register(DeferredRegister.Items itemRegister, Supplier<Item.Properties> settings) {
        this.item = itemRegister.register(this.name, () -> new BankItem(settings.get().stacksTo(1), this));
    }

    public int size() {
        return this.rows * this.cols;
    }

    public String getName() {
        return this.name;
    }

    public static BankType getBankTypeFromName(String name) {
        for (BankType bankType : BankStorage.BANK_TYPES) {
            if (bankType.getName().equals(name)) {
                return bankType;
            }
        }

        throw new Error("Cannot get BankType of name '" + name + "'");
    }

    @Override
    public boolean equals(Object other) {
        return this == other;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
