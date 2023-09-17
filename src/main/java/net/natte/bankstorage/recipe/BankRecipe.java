package net.natte.bankstorage.recipe;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.natte.bankstorage.item.BankItem;

public class BankRecipe extends ShapedRecipe {


    public BankRecipe(ShapedRecipe recipe) {
        super(recipe.getId(), "bank_upgrade", recipe.getCategory(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getOutput(null));

    }

    @Override
    public ItemStack craft(RecipeInputInventory recipeInputInventory, DynamicRegistryManager dynamicRegistryManager) {
        Optional<ItemStack> maybeBankItemStack = recipeInputInventory.getInputStacks().stream()
                .filter(stack -> (stack.getItem() instanceof BankItem)).findFirst();

        if (maybeBankItemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = super.craft(recipeInputInventory, dynamicRegistryManager);
        if (maybeBankItemStack.get().hasNbt()) {
            NbtCompound nbt = maybeBankItemStack.get().getNbt();
            if (nbt != null && nbt.contains(BankItem.UUID_KEY)) {
                UUID uuid = nbt.getUuid(BankItem.UUID_KEY);
                result.getOrCreateNbt().putUuid(BankItem.UUID_KEY, uuid);

            }
        }

        return result;
    }

}
