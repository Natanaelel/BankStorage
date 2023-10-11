package net.natte.bankstorage.screen;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.natte.bankstorage.item.CachedBankStorage;

public class BankScreenListener implements ScreenHandlerListener {

    @Override
    public void onSlotUpdate(ScreenHandler screenHandler, int var2, ItemStack var3) {
        // TODO Auto-generated method stub
        // CachedBankStorage.
        System.out.println("screen listener onSlotUpdate");
        // System.out.println(screenHandler.syncState());
    }
    
    @Override
    public void onPropertyUpdate(ScreenHandler var1, int var2, int var3) {
        System.out.println("screen listener onPropertyUpdate");
        // TODO Auto-generated method stub
    }

}
