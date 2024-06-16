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

import java.util.Optional;

public class BankUpgradeRecipe extends ShapedRecipe {

    public BankUpgradeRecipe(ShapedRecipe recipe) {
        super(recipe.getGroup(), recipe.category(), recipe.pattern, recipe.result);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }


    @Override
    public ItemStack assemble(CraftingInput recipeInputInventory, HolderLookup.Provider registryLookup) {
        Optional<ItemStack> maybeBankItemStack = recipeInputInventory.items().stream()
                .filter(Util::isBank).findFirst();

        if (maybeBankItemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = super.assemble(recipeInputInventory, registryLookup);
        result.applyComponentsAndValidate(maybeBankItemStack.get().getComponentsPatch());

        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BankStorage.BANK_UPGRADE_RECIPE_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<BankUpgradeRecipe> {
        public static final MapCodec<BankUpgradeRecipe> CODEC = ShapedRecipe.Serializer.CODEC.xmap(BankUpgradeRecipe::new, ShapedRecipe.class::cast);
        public static final StreamCodec<RegistryFriendlyByteBuf, BankUpgradeRecipe> STREAM_CODEC = ShapedRecipe.Serializer.STREAM_CODEC.map(BankUpgradeRecipe::new, ShapedRecipe.class::cast);

        @Override
        public MapCodec<BankUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BankUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
