package net.natte.bankstorage.recipe;

import java.util.Optional;

import com.google.gson.JsonObject;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.item.LinkItem;
import net.natte.bankstorage.util.Util;

public class BankLinkRecipe extends ShapedRecipe {

    public BankLinkRecipe(ShapedRecipe recipe) {
        super(recipe.getId(), "bank_link", recipe.getCategory(), recipe.getWidth(), recipe.getHeight(),
                recipe.getIngredients(), recipe.getOutput(null));
    }

    @Override
    public ItemStack craft(RecipeInputInventory recipeInputInventory, DynamicRegistryManager dynamicRegistryManager) {
        Optional<ItemStack> maybeBankItemStack = recipeInputInventory.getInputStacks().stream()
                .filter(stack -> (stack.getItem() instanceof BankItem)).findFirst();

        if (maybeBankItemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack bank = maybeBankItemStack.get();
        ItemStack result = super.craft(recipeInputInventory, dynamicRegistryManager);
        result.setNbt(bank.getNbt());
        result.getNbt().putString(LinkItem.BANK_TYPE_KEY, ((BankItem) bank.getItem()).getType().getName());
        return result;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(RecipeInputInventory recipeInputInventory) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(recipeInputInventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack stack = recipeInputInventory.getStack(i);
            if (Util.isBank(stack))
                defaultedList.set(i, stack.copyWithCount(1));
        }
        return defaultedList;
    }

    public static class Serializer extends ShapedRecipe.Serializer {

        @Override
        public BankLinkRecipe read(Identifier id, JsonObject json) {
            return new BankLinkRecipe(super.read(id, json));
        }

        @Override
        public BankLinkRecipe read(Identifier id, PacketByteBuf buf) {
            return new BankLinkRecipe(super.read(id, buf));

        }
    }

}
