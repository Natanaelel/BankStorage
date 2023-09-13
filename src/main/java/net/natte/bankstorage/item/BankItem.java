package net.natte.bankstorage.item;

import java.util.List;
import java.util.UUID;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.world.BankStateSaverAndLoader;

public class BankItem extends Item {

    public BankItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemStack bank = user.getStackInHand(hand);
        if(!world.isClient){
            BankItemStorage bankItemStorage = getBankItemStorage(bank, world);
            user.openHandledScreen(bankItemStorage);
        }

        return super.use(world, user, hand);
    }

    private BankItemStorage getBankItemStorage(ItemStack bank, World world) {

        NbtCompound nbt = bank.getOrCreateNbt();
        UUID uuid;
        if(nbt.contains("uuid")){
            uuid = nbt.getUuid("uuid");
        }else{
            uuid = UUID.randomUUID();
            nbt.putUuid("uuid", uuid);
        }

        BankStateSaverAndLoader serverState = BankStateSaverAndLoader.getServerState(world.getServer());
        BankItemStorage bankItemStorage = serverState.getOrCreate(uuid);
        return bankItemStorage;

    }


    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        // TODO Auto-generated method stub
        // if(stack.hasNbt())
        if(stack.hasNbt() && stack.getNbt().contains("uuid")) tooltip.add(Text.of(stack.getNbt().getUuid("uuid").toString()));
        // tooltip.add(Text.of(stack.getOrCreateNbt().get));
        super.appendTooltip(stack, world, tooltip, context);
    }

}
