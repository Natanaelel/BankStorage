package net.natte.bankstorage.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.util.Util;

import java.util.Optional;

public class BankLinkRecipe extends ShapedRecipe {

    public BankLinkRecipe(ShapedRecipe recipe) {
        super(recipe.getGroup(), recipe.category(), recipe.pattern, recipe.result);
    }

    @Override
    public ItemStack assemble(CraftingInput recipeInputInventory, HolderLookup.Provider registryLookup) {
        Optional<ItemStack> maybeBankItemStack = recipeInputInventory.items().stream()
                .filter(Util::isBankLike).findFirst();

        if (maybeBankItemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack bank = maybeBankItemStack.get();
        if (!Util.hasUUID(bank)) {
            return ItemStack.EMPTY;
        }
        ItemStack result = super.assemble(recipeInputInventory, registryLookup);
        result.applyComponents(bank.getComponentsPatch());
        result.set(BankStorage.BankTypeComponentType, Util.getType(bank));
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput recipeInputInventory) {
        NonNullList<ItemStack> remainingItems = super.getRemainingItems(recipeInputInventory);
        for (int i = 0; i < remainingItems.size(); ++i) {
            ItemStack stack = recipeInputInventory.getItem(i);
            if (Util.isBankLike(stack))
                remainingItems.set(i, stack.copyWithCount(1));
        }
        return remainingItems;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BankStorage.BANK_LINK_RECIPE_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<BankLinkRecipe> {
        public static final MapCodec<BankLinkRecipe> CODEC = ShapedRecipe.Serializer.CODEC.xmap(BankLinkRecipe::new, ShapedRecipe.class::cast);
        public static final StreamCodec<RegistryFriendlyByteBuf, BankLinkRecipe> STREAM_CODEC = ShapedRecipe.Serializer.STREAM_CODEC.map(BankLinkRecipe::new, ShapedRecipe.class::cast);

        @Override
        public MapCodec<BankLinkRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BankLinkRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
