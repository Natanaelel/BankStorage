package net.natte.bankstorage.container;

import java.util.List;

import net.minecraft.item.Item.Settings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;

public class BankType {

    private String name;
    public int rows;
    public int cols;
    public int guiTextureWidth;
    public int guiTextureHeight;

    private ScreenHandlerType<BankScreenHandler> screenHandlerType;

    public int slotStorageMultiplier;
    public BankItem item;

    public BankType(String name, int slotStorageMultiplier, int rows, int cols, int guiTextureWidth,
            int guiTextureHeight) {
        this.name = name;
        this.rows = rows;
        this.cols = cols;

        this.guiTextureWidth = guiTextureWidth;
        this.guiTextureHeight = guiTextureHeight;

        this.slotStorageMultiplier = slotStorageMultiplier;

    }

    public void register(List<BankType> types) {
        register(types, new Settings());
    }

    public void register(List<BankType> types, Settings settings) {
        this.item = new BankItem(settings.maxCount(1), this);
        Identifier identifier = Util.ID(this.name);
        Registry.register(Registries.ITEM, identifier, this.item);
        this.screenHandlerType = new ScreenHandlerType<>(BankScreenHandler.fromType(this),
                FeatureFlags.VANILLA_FEATURES);
        Registry.register(Registries.SCREEN_HANDLER, identifier, screenHandlerType);
        types.add(this);
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
}
