package net.natte.bankstorage.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.util.Util;

public class BankRecipe extends ShapedRecipe {

    BankRecipe(ShapedRecipe shapedRecipe) {
        super(shapedRecipe.getGroup(), shapedRecipe.category(), shapedRecipe.pattern, shapedRecipe.result);
    }

    @Override
    public ItemStack assemble(CraftingInput recipeInputInventory, HolderLookup.Provider registryLookup) {

        ItemStack result = super.assemble(recipeInputInventory, registryLookup);

        recipeInputInventory
                .items()
                .stream()
                .filter(Util::isBank)
                .findFirst()
                .ifPresent(bank -> result.applyComponentsAndValidate(bank.getComponentsPatch()));

        Util.getOrSetUUID(result);

        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BankStorage.BANK_RECIPE_SERIALIZER.get();
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
