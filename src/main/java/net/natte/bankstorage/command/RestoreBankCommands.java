package net.natte.bankstorage.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.command.SortingModeArgumentType.SortingMode;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.state.BankStateManager;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.*;
import java.util.stream.Stream;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class RestoreBankCommands {

    // per player map of filters
    private static final Map<UUID, BankFilter> filters = new HashMap<>();

    private static final UUID TERMINAL_UUID = UUID.randomUUID();


    private static final ArgumentTypeInfo<SortingModeArgumentType,?> SORTING_MODE_ARGUMENT_TYPE = SingletonArgumentInfo.contextFree(SortingModeArgumentType::sortingMode);
    private static final ArgumentTypeInfo<BankTypeArgumentType,?> BANK_TYPE_ARGUMENT_TYPE = SingletonArgumentInfo.contextFree(BankTypeArgumentType::bankType);

    public static void registerCommands(RegisterCommandsEvent event) {

        event.getDispatcher().register(literal("bankstorage")
                .requires(context -> context.hasPermission(2))
                .then(literal("fromuuid")
                        .then(argument("uuid", UuidArgument.uuid())
                                .then(argument("player", EntityArgument.player())
                                        .executes(RestoreBankCommands::restoreBankCommand))))
                .then(literal("list").executes(RestoreBankCommands::listBankStorages).then(
                        literal("sort").then(argument("sorting_mode", SortingModeArgumentType.sortingMode())
                                .executes(RestoreBankCommands::listBankStoragesSorted))))
                .then(literal("filter")
                        .then(literal("clear")
                                .executes(RestoreBankCommands::clearFilter)
                                .then(literal("type")
                                        .executes(RestoreBankCommands::clearFilterType))
                                .then(literal("player")
                                        .executes(RestoreBankCommands::clearPlayerFilter))
                                .then(literal("item")
                                        .executes(RestoreBankCommands::clearItemFilter)))
                        .then(literal("add")
                                .then(literal("type").then(argument("type", BankTypeArgumentType.bankType())
                                        .executes(RestoreBankCommands::addTypeFilter)))
                                .then(literal("player").then(argument("player", EntityArgument.player())
                                        .executes(RestoreBankCommands::addPlayerFilter)))
                                .then(literal("item").then(argument("itemPredicate",
                                        ItemPredicateArgument.itemPredicate(event.getBuildContext()))
                                        .executes(RestoreBankCommands::addItemFilter))))));
    }

    public static void registerArgumentTypes() {

        BankStorage.COMMAND_ARGUMENT_TYPES.register("sorting_mode", () -> SORTING_MODE_ARGUMENT_TYPE);
        BankStorage.COMMAND_ARGUMENT_TYPES.register("bank_type", () -> BANK_TYPE_ARGUMENT_TYPE);

        ArgumentTypeInfos.registerByClass(SortingModeArgumentType.class, SORTING_MODE_ARGUMENT_TYPE);
        ArgumentTypeInfos.registerByClass(BankTypeArgumentType.class, BANK_TYPE_ARGUMENT_TYPE);

    }

    private static int restoreBankCommand(CommandContext<CommandSourceStack> context) {

        ServerPlayer player;
        try {
            player = EntityArgument.getPlayer(context, "player");
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.translatable("admin_restore_bank.bankstorage.unknown_player"));
            return 0;
        }
        UUID uuid = UuidArgument.getUuid(context, "uuid");
        BankItemStorage bank = Util.getBankItemStorage(uuid);
        if (bank == null) {
            context.getSource().sendSuccess(() -> Component.translatable("command.bankstorage.unknownid"), false);
            return 0;
        }
        ItemStack stack = BuiltInRegistries.ITEM
                .get(Util.ID(bank.type.getName()))
                .getDefaultInstance();
        stack.set(BankStorage.UUIDComponentType, uuid);
        player.getInventory().add(stack);
        return 1;
    }

    private static int listBankStorages(CommandContext<CommandSourceStack> context) {

        BankFilter filter = getFilter(context);

        List<BankItemStorage> bankItemStorages = BankStateManager
                .getState()
                .getBankItemStorages()
                .stream()
                .filter(filter::matchesBank)
                .toList();

        listBankStoragesInChat(context, bankItemStorages);

        return 1;
    }

    private static int listBankStoragesSorted(CommandContext<CommandSourceStack> context) {

        BankFilter filter = getFilter(context);
        SortingMode sortingMode = SortingModeArgumentType.getSortingMode(context, "sorting_mode");

        Stream<BankItemStorage> bankItemStorages = BankStateManager
                .getState()
                .getBankItemStorages()
                .stream()
                .filter(filter::matchesBank);

        List<BankItemStorage> sortedBankItemStorages = switch (sortingMode) {
            case DATE -> bankItemStorages.sorted(Comparator.comparing(bank -> bank.dateCreated)).toList();
            case TYPE -> bankItemStorages.sorted(Comparator.comparing(bank -> bank.type.getName())).toList();
            case PLAYER -> bankItemStorages.sorted(Comparator.comparing(bank -> bank.usedByPlayerUUID)).toList();
        };

        listBankStoragesInChat(context, sortedBankItemStorages);

        return 1;
    }

    private static int listBankStoragesInChat(CommandContext<CommandSourceStack> context,
                                              List<BankItemStorage> bankItemStorages) {

        ServerPlayer player = context.getSource().getPlayer();

        MutableComponent message = Component.empty();

        for (BankItemStorage bankItemStorage : bankItemStorages) {
            UUID uuid = bankItemStorage.uuid;

            long nonEmptyStacks = bankItemStorage.getItems().stream().filter(stack -> !stack.isEmpty()).count();

            String command = "/bankstorage fromuuid " + uuid.toString() + " " + player.getName().getString();

            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("command.bankstorage.hoverinfo"));

            message.append(Component
                    .literal(bankItemStorage.type.getName() + ", " + nonEmptyStacks + " items, uuid: "
                            + bankItemStorage.uuid.toString() + ", " + bankItemStorage.usedByPlayerName
                            + "\n")
                    .withStyle(style -> style
                            .withClickEvent(clickEvent)
                            .withHoverEvent(hoverEvent)));
        }
        context.getSource().sendSystemMessage(message);
        context.getSource().sendSystemMessage(
                Component.translatable("admin_restore_bank.bankstorage.listed_banks_num", bankItemStorages.size()));

        return 1;
    }

    private static BankFilter getFilter(CommandContext<CommandSourceStack> context) {
        UUID uuid =
                context.getSource().isPlayer() ? context.getSource().getPlayer().getUUID() : TERMINAL_UUID;
        return filters.computeIfAbsent(uuid, ignoredUuid -> new BankFilter());
    }

    private static int clearFilter(CommandContext<CommandSourceStack> context) {
        getFilter(context).clear();
        context.getSource().sendSystemMessage(Component.translatable("admin_restore_bank.bankstorage.cleared_all_filters"));
        return 1;
    }

    private static int clearFilterType(CommandContext<CommandSourceStack> context) {
        getFilter(context).clearType();
        context.getSource().sendSystemMessage(Component.translatable("admin_restore_bank.bankstorage.cleared_type_filters"));
        return 1;
    }

    private static int clearPlayerFilter(CommandContext<CommandSourceStack> context) {
        getFilter(context).clearPlayers();
        context.getSource().sendSystemMessage(Component.translatable("admin_restore_bank.bankstorage.cleared_player_filters"));
        return 1;
    }

    private static int clearItemFilter(CommandContext<CommandSourceStack> context) {
        getFilter(context).clearItems();
        context.getSource().sendSystemMessage(Component.translatable("admin_restore_bank.bankstorage.cleared_item_filters"));
        return 1;
    }

    private static int addTypeFilter(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        getFilter(context).addType(BankTypeArgumentType.getBankType(context, "type"));
        context.getSource().sendSystemMessage(Component.translatable("admin_restore_bank.bankstorage.added_type_filter"));
        return 1;
    }

    private static int addPlayerFilter(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        getFilter(context).addPlayer(EntityArgument.getPlayer(context, "player"));
        context.getSource().sendSystemMessage(Component.translatable("admin_restore_bank.bankstorage.added_player_filter"));
        return 1;
    }

    private static int addItemFilter(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        getFilter(context).addItemPredicate(ItemPredicateArgument.getItemPredicate(context, "itemPredicate"));
        context.getSource().sendSystemMessage(Component.translatable("admin_restore_bank.bankstorage.added_item_filter"));
        return 1;
    }
}
