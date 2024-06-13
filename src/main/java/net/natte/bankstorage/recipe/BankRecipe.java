package net.natte.bankstorage.recipe;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.natte.bankstorage.item.BankItem;

import java.util.Optional;

public class BankRecipe extends ShapedRecipe {

    public static final Serializer SERIALIZER = new Serializer();

    public BankRecipe(ShapedRecipe recipe) {
        super(recipe.getGroup(), recipe.category(), recipe.pattern, recipe.result);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer recipeInputInventory, HolderLookup.Provider registryLookup) {
        Optional<ItemStack> maybeBankItemStack = recipeInputInventory.getItems().stream()
                .filter(stack -> (stack.getItem() instanceof BankItem)).findFirst();

        if (maybeBankItemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = super.assemble(recipeInputInventory, registryLookup);
        result.applyComponentsAndValidate(maybeBankItemStack.get().getComponentsPatch());

        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static class Serializer implements RecipeSerializer<BankRecipe> {
        public static final MapCodec<BankRecipe> CODEC = ShapedRecipe.Serializer.CODEC.xmap(BankRecipe::new, ShapedRecipe.class::cast);
        public static final StreamCodec<RegistryFriendlyByteBuf, BankRecipe> STREAM_CODEC = ShapedRecipe.Serializer.STREAM_CODEC.map(BankRecipe::new, ShapedRecipe.class::cast);

        @Override
        public MapCodec<BankRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BankRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
