package net.natte.bankstorage.container;

import java.util.List;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;

public class BankType {

    public static final Codec<BankType> CODEC = Codec.STRING.xmap(BankType::getBankTypeFromName, BankType::getName);
    public static final PacketCodec<ByteBuf, BankType> PACKET_CODEC = PacketCodecs.STRING.xmap(BankType::getBankTypeFromName, BankType::getName);

    private String name;
    public int rows;
    public int cols;
    public int guiTextureWidth;
    public int guiTextureHeight;

    private ScreenHandlerType<BankScreenHandler> screenHandlerType;

    public int stackLimit;
    public BankItem item;

    public BankType(String name, int stackLimit, int rows, int cols, int guiTextureWidth,
            int guiTextureHeight) {
        this.name = name;
        this.rows = rows;
        this.cols = cols;

        this.guiTextureWidth = guiTextureWidth;
        this.guiTextureHeight = guiTextureHeight;

        this.stackLimit = stackLimit;

    }

    public void register(List<BankType> types) {
        register(types, new Settings());
    }

    public void register(List<BankType> types, Settings settings) {
        this.item = new BankItem(settings.maxCount(1), this);
        Identifier identifier = Util.ID(this.name);
        Registry.register(Registries.ITEM, identifier, this.item);
        this.screenHandlerType = new ExtendedScreenHandlerType<BankScreenHandler, ItemStack>(BankScreenHandler.fromType(this), ItemStack.PACKET_CODEC);
        Registry.register(Registries.SCREEN_HANDLER, identifier, screenHandlerType);
        types.add(this);
        CauldronBehavior.WATER_CAULDRON_BEHAVIOR.map().put(this.item, CauldronBehavior.CLEAN_DYEABLE_ITEM);
    }

    public int size() {
        return this.rows * this.cols;
    }

    public String getName() {
        return this.name;
    }

    public ScreenHandlerType<BankScreenHandler> getScreenHandlerType() {
        return this.screenHandlerType;
    }

    public Identifier getGuiTexture() {
        return Util.ID("textures/gui/" + this.cols + "x" + this.rows + ".png");
    }

    public static BankType getBankTypeFromName(String name) {
        for (BankType bankType : BankStorage.bankTypes) {
            if (bankType.getName().equals(name)) {
                return bankType;
            }
        }

        throw new Error("Cannot get BankType of name '" + name + "'");
    }
}
