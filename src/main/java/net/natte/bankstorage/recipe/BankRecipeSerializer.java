package net.natte.bankstorage.recipe;

import com.google.gson.JsonObject;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;

public class BankRecipeSerializer extends ShapedRecipe.Serializer {

    @Override
    public BankRecipe read(Identifier id, JsonObject json) {
        return new BankRecipe(super.read(id, json));
    }

    @Override
    public BankRecipe read(Identifier id, PacketByteBuf buf) {
        return new BankRecipe(super.read(id, buf));

    }
}
