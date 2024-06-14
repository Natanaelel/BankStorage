package net.natte.bankstorage.container;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

public class BankType {

    public static final Codec<BankType> CODEC = Codec.STRING.xmap(BankType::getBankTypeFromName, BankType::getName);
    public static final StreamCodec<ByteBuf, BankType> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(BankType::getBankTypeFromName, BankType::getName);

    private String name;
    public int rows;
    public int cols;
    public int guiTextureWidth;
    public int guiTextureHeight;

    public int stackLimit;
    public DeferredHolder<Item, BankItem> item;

    public BankType(String name, int stackLimit, int rows, int cols, int guiTextureWidth,
                    int guiTextureHeight) {
        this.name = name;
        this.rows = rows;
        this.cols = cols;

        this.guiTextureWidth = guiTextureWidth;
        this.guiTextureHeight = guiTextureHeight;

        this.stackLimit = stackLimit;

    }

    public void register() {
        register(Item.Properties::new);
    }

    public void register(Supplier<Item.Properties> settings) {
        this.item = BankStorage.ITEMS.register(this.name, () -> new BankItem(settings.get().stacksTo(1), this));
    }

    public int size() {
        return this.rows * this.cols;
    }

    public String getName() {
        return this.name;
    }

    public ResourceLocation getGuiTexture() {
        return Util.ID("textures/gui/" + this.cols + "x" + this.rows + ".png");
    }

    public static BankType getBankTypeFromName(String name) {
        for (BankType bankType : BankStorage.BANK_TYPES) {
            if (bankType.getName().equals(name)) {
                return bankType;
            }
        }

        throw new Error("Cannot get BankType of name '" + name + "'");
    }
}
