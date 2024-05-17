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
import net.minecraft.util.collection.DefaultedList;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.util.Util;

public class BankLinkRecipe extends ShapedRecipe {

    public BankLinkRecipe(ShapedRecipe recipe) {
        super(recipe.getGroup(), recipe.getCategory(), recipe.raw, recipe.getResult(null));
    }

    @Override
    public ItemStack craft(RecipeInputInventory recipeInputInventory, WrapperLookup registryLookup) {
        Optional<ItemStack> maybeBankItemStack = recipeInputInventory.getHeldStacks().stream()
                .filter(stack -> Util.isBank(stack)).findFirst();

        if (maybeBankItemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack bank = maybeBankItemStack.get();
        if (!Util.hasUUID(bank)) {
            return ItemStack.EMPTY;
        }
        ItemStack result = super.craft(recipeInputInventory, registryLookup);
        result.applyChanges(bank.getComponentChanges());
        result.set(BankStorage.BankTypeComponentType, ((BankItem) bank.getItem()).getType());
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

    public static class Serializer implements RecipeSerializer<BankLinkRecipe> {
        public static final MapCodec<BankLinkRecipe> CODEC = ShapedRecipe.Serializer.CODEC.xmap(BankLinkRecipe::new, ShapedRecipe.class::cast);
        public static final PacketCodec<RegistryByteBuf, BankLinkRecipe> PACKET_CODEC = ShapedRecipe.Serializer.PACKET_CODEC.xmap(BankLinkRecipe::new, ShapedRecipe.class::cast);

        @Override
        public MapCodec<BankLinkRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, BankLinkRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
