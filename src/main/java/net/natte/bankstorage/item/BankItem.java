package net.natte.bankstorage.item;

import java.util.List;
import java.util.UUID;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.world.BankStateSaverAndLoader;

public class BankItem extends Item {

    public static final String UUID_KEY = "bank:uuid";
    
    private BankType type;

    public BankItem(Settings settings, BankType type) {
        super(settings);
        this.type = type;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemStack bank = user.getStackInHand(hand);
        if (!world.isClient) {
            if(bank.getCount() == 1){
                BankItemStorage bankItemStorage = BankItem.getBankItemStorage(bank, world);
                user.openHandledScreen(bankItemStorage);
            }
        }

        return super.use(world, user, hand);
    }

    public BankType getType() {
        return this.type;
    }

    public static BankItemStorage getBankItemStorage(ItemStack bank, World world) {

        NbtCompound nbt = bank.getOrCreateNbt();
        UUID uuid;
        if (nbt.contains(UUID_KEY)) {
            uuid = nbt.getUuid(UUID_KEY);
        } else {
            uuid = UUID.randomUUID();
            nbt.putUuid(UUID_KEY, uuid);
        }

        BankType type = ((BankItem) bank.getItem()).getType();
        BankStateSaverAndLoader serverState = BankStateSaverAndLoader.getServerState(world.getServer());
        BankItemStorage bankItemStorage = serverState.getOrCreate(uuid, type, bank.getName());
        return bankItemStorage;

    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {

        if (context.isAdvanced())
            if (stack.hasNbt() && stack.getNbt().contains(UUID_KEY))
                tooltip.add(Text.literal(stack.getNbt().getUuid(UUID_KEY).toString()).formatted(Formatting.GRAY));

        super.appendTooltip(stack, world, tooltip, context);
    }

}
