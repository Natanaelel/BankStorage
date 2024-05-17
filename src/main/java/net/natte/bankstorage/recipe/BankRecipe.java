package net.natte.bankstorage.recipe;

import java.util.Optional;

import com.mojang.serialization.MapCodec;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.natte.bankstorage.item.BankItem;

public class BankRecipe extends ShapedRecipe {

    public BankRecipe(ShapedRecipe recipe) {
        super(recipe.getGroup(), recipe.getCategory(), recipe.raw, recipe.getResult(null));
    }

    @Override
    public ItemStack craft(RecipeInputInventory recipeInputInventory, WrapperLookup registryLookup) {
        Optional<ItemStack> maybeBankItemStack = recipeInputInventory.getHeldStacks().stream()
                .filter(stack -> (stack.getItem() instanceof BankItem)).findFirst();

        if (maybeBankItemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = super.craft(recipeInputInventory, registryLookup);
        result.applyChanges(maybeBankItemStack.get().getComponentChanges());

        return result;
    }

    public static class Serializer implements RecipeSerializer<BankRecipe> {
        public static final MapCodec<BankRecipe> CODEC = ShapedRecipe.Serializer.CODEC.xmap(BankRecipe::new, ShapedRecipe.class::cast);
        public static final PacketCodec<RegistryByteBuf, BankRecipe> PACKET_CODEC = ShapedRecipe.Serializer.PACKET_CODEC.xmap(BankRecipe::new, ShapedRecipe.class::cast);

        @Override
        public MapCodec<BankRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, BankRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
