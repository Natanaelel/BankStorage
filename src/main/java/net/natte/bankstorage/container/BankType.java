package net.natte.bankstorage.container;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.screen.BankScreenHandler;

public class BankType {
    
    private String name;
    public int rows;
    public int cols;
    public int guiXOffset;
    public int guiYOffset;
    public int titleXOffset;
    public int titleYOffset;
    public int guiTextureWidth;
    public int guiTextureHeight;

    
    private ScreenHandlerType<BankScreenHandler> screenHandlerType;

    public int slotStorageMultiplier;
    
    public BankType(String name, int slotStorageMultiplier, int rows, int cols, int guiXOffset, int guiYOffset, int titleXOffset, int titleYOffset, int guiTextureWidth, int guiTextureHeight){
        this.name = name;
        this.rows = rows;
        this.cols = cols;

        this.guiXOffset = guiXOffset;
        this.guiYOffset = guiYOffset;
        this.titleXOffset = titleXOffset;
        this.titleYOffset = titleYOffset;

        this.guiTextureWidth = guiTextureWidth;
        this.guiTextureHeight = guiTextureHeight;

        this.slotStorageMultiplier = slotStorageMultiplier;

    }

    public void register(List<BankType> types){
        
        Item bankItem = new BankItem(new Settings().maxCount(1), this);
        Identifier identifier = new Identifier(BankStorage.MOD_ID, this.name);
        Registry.register(Registries.ITEM, identifier, bankItem);
        this.screenHandlerType = new ScreenHandlerType<>(BankScreenHandler.fromType(this), FeatureFlags.VANILLA_FEATURES);
        Registry.register(Registries.SCREEN_HANDLER, identifier, screenHandlerType);

        types.add(this);
    }

    public int size(){
        return this.rows * this.cols;
    }

    public String getName(){
        return this.name;
    }

    public ScreenHandlerType<BankScreenHandler> getScreenHandlerType(){
        return this.screenHandlerType;
    }

    public Identifier getGuiTexture(){
        return new Identifier(BankStorage.MOD_ID, "textures/gui/" + this.name + ".png");
    }

}
