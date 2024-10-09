package net.natte.bankstorage.container;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.item.Item.Settings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;

public class BankType {

    private final String name;
    public final int rows;
    public final int cols;

    private ScreenHandlerType<BankScreenHandler> screenHandlerType;

    public int stackLimit;
    public BankItem item;

    public BankType(String name, int stackLimit, int rows, int cols) {
        this.name = name;
        this.rows = rows;
        this.cols = cols;

        this.stackLimit = stackLimit;

    }

    public void register() {
        register(new Settings());
    }

    public void register(Settings settings) {
        this.item = new BankItem(settings.maxCount(1), this);
        Identifier identifier = Util.ID(this.name);
        Registry.register(Registries.ITEM, identifier, this.item);
        this.screenHandlerType = new ExtendedScreenHandlerType<>(BankScreenHandler.fromType(this));
        Registry.register(Registries.SCREEN_HANDLER, identifier, screenHandlerType);
        CauldronBehavior.WATER_CAULDRON_BEHAVIOR.put(this.item, CauldronBehavior.CLEAN_DYEABLE_ITEM);
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

}
