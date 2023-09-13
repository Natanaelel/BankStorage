package net.natte.bankstorage.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.natte.bankstorage.BankStorage;


public class ItemModelProvider extends FabricModelProvider {


    public ItemModelProvider(FabricDataOutput generator) {
        super(generator);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
   
        itemModelGenerator.register(BankStorage.BANK_ITEM, Models.CUBE_ALL);
    }

 

}