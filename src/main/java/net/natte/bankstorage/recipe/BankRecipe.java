package net.natte.bankstorage.recipe;

import com.google.gson.JsonObject;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.util.Util;

public class BankRecipe extends ShapedRecipe {

    public BankRecipe(ShapedRecipe recipe) {
        super(recipe.getId(), "copy_nbt_or_assign_uuid", recipe.getCategory(), recipe.getWidth(), recipe.getHeight(),
                recipe.getIngredients(), recipe.getOutput(null));
    }

    @Override
    public ItemStack craft(RecipeInputInventory recipeInputInventory, DynamicRegistryManager dynamicRegistryManager) {

        ItemStack result = super.craft(recipeInputInventory, dynamicRegistryManager);

        recipeInputInventory
                .getInputStacks()
                .stream()
                .filter(Util::isBank)
                .findFirst()
                .ifPresent(bank -> result.setNbt(bank.getNbt()));

        Util.getOrSetUUID(result);

        return result;
    }

    public static class Serializer extends ShapedRecipe.Serializer {

        @Override
        public BankRecipe read(Identifier id, JsonObject json) {
            return new BankRecipe(super.read(id, json));
        }

        @Override
        public BankRecipe read(Identifier id, PacketByteBuf buf) {
            return new BankRecipe(super.read(id, buf));

        }
    }

}
