package net.natte.bankstorage.item;

import java.util.List;
import java.util.UUID;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
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
            if (bank.getCount() == 1) {
                BankItemStorage bankItemStorage = BankItem.getBankItemStorage(bank, world);
                if (bankItemStorage.options.buildMode == BuildMode.NONE) {
                    user.openHandledScreen(bankItemStorage);
                    return TypedActionResult.success(bank);
                }
                if (bankItemStorage.options.buildMode == BuildMode.NORMAL) {
                    List<ItemStack> items = bankItemStorage.getUniqueItems();
                    ItemStack itemStack = items.get(bankItemStorage.selectedItemSlot % items.size());
                    System.out.println("use " + itemStack);
                    user.setStackInHand(hand, itemStack);
                    TypedActionResult<ItemStack> useResult = itemStack.use(world, user, hand);
                    user.setStackInHand(hand, bank);
                    // if(useResult.getResult() == ActionResult.)
                    // if(useResult.getResult().isAccepted())
                    return new TypedActionResult<ItemStack>(useResult.getResult(), bank);
                    // return ;
                }
            }
        }

        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // System.out.println("use on block");
        PlayerEntity user = context.getPlayer();
        World world = context.getWorld();
        ItemStack bank = user.getStackInHand(user.getActiveHand());
        if (world.isClient) {
            return ActionResult.PASS;
        }
        if (bank.getCount() == 1) {
            BankOptions options;
            // if (world.isClient) {
            //     CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(bank);
            //     options = cachedBankStorage == null ? new BankOptions() : cachedBankStorage.options;
            // } else {
            BankItemStorage bankItemStorage = BankItem.getBankItemStorage(bank, world);
            options = bankItemStorage.options;
            // }
            System.out.println(world.isClient ? "client" : "server");
            System.out.println(options.buildMode);
            if (options.buildMode == BuildMode.NORMAL) {
                ItemStack itemStack;
                // if (world.isClient) {
                //     CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(bank);
                //     itemStack = cachedBankStorage == null ? ItemStack.EMPTY : cachedBankStorage.getSelectedItem();
                // } else {
                // BankItemStorage bankItemStorage = BankItem.getBankItemStorage(bank, world);
                List<ItemStack> items = bankItemStorage.getUniqueItems();
                itemStack = items.get(bankItemStorage.selectedItemSlot % items.size());
                // }
                // itemStack = BuildModePreviewRenderer.Instance.stackInHand;
                // BuildModePreviewRenderer.Instance
                System.out.println(world.isClient ? "client" : "server");
                System.out.println("use on block " + itemStack);
                user.setStackInHand(context.getHand(), itemStack);
                ActionResult useResult = itemStack
                        .useOnBlock(new ItemUsageContext(world, user, context.getHand(), itemStack, context.hit));
                user.setStackInHand(context.getHand(), bank);
                System.out.println("stack in hand: " + user.getStackInHand(user.getActiveHand()));
                // if(useResult.getResult() == ActionResult.)
                return useResult;
            }
        }
        // }
        return super.useOnBlock(context);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        // World world = user.getWorld();
        // ItemStack bank = user.getStackInHand(hand);
        // if (!world.isClient) {
        // if(bank.getCount() == 1){
        // BankItemStorage bankItemStorage = BankItem.getBankItemStorage(bank, world);
        // if(bankItemStorage.options.buildMode == BuildMode.NONE){
        // user.openHandledScreen(bankItemStorage);
        // return ActionResult.SUCCESS;
        // }
        // if(bankItemStorage.options.buildMode == BuildMode.NORMAL){
        // List<ItemStack> items = bankItemStorage.getUniqueItems();
        // ItemStack itemStack = items.get(bankItemStorage.selectedItemSlot %
        // items.size());
        // System.out.println("use on entity" + itemStack);
        // user.setStackInHand(hand, itemStack);
        // ActionResult useResult = itemStack.useOnEntity(user, entity, hand);
        // user.setStackInHand(hand, bank);
        // // if(useResult.getResult() == ActionResult.)
        // return useResult;
        // }
        // }
        // }

        // return super.use(world, user, hand);
        return super.useOnEntity(stack, user, entity, hand);
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
        // bankItemStorage.options = BankOptions.fromNbt(nbt);
        return bankItemStorage;

    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {

        if (context.isAdvanced())
            if (stack.hasNbt() && stack.getNbt().contains(UUID_KEY))
                tooltip.add(Text.literal(stack.getNbt().getUuid(UUID_KEY).toString()).formatted(Formatting.GRAY));

        super.appendTooltip(stack, world, tooltip, context);
    }

    public static BankOptions getOrCreateOptions(ItemStack itemStack) {
        return BankOptions.fromNbt(itemStack.getOrCreateNbt());
    }

}
