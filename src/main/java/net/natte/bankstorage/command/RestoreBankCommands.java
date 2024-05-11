package net.natte.bankstorage.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.Comparator;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.command.SortingModeArgumentType.SortingMode;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.util.Util;
import net.natte.bankstorage.world.BankStateSaverAndLoader;

public class RestoreBankCommands {

    private static Map<UUID, BankFilter> filters = new HashMap<>();

    public static void register() {

        registerArgumentTypes();
        registerCommands();

    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("bankstorage")
                    .requires(context -> context.hasPermissionLevel(2))
                    .then(literal("fromuuid")
                            .then(argument("uuid", UuidArgumentType.uuid())
                                    .then(argument("player", EntityArgumentType.player())
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
                                    .then(literal("player").then(argument("player", EntityArgumentType.player())
                                            .executes(RestoreBankCommands::addPlayerFilter)))
                                    .then(literal("item").then(argument("itemPredicate",
                                            ItemPredicateArgumentType.itemPredicate(registryAccess))
                                            .executes(RestoreBankCommands::addItemFilter))))));
        });
    }

    private static void registerArgumentTypes() {
        ArgumentTypeRegistry.registerArgumentType(Util.ID("sorting_mode"),
                SortingModeArgumentType.class, ConstantArgumentSerializer.of(SortingModeArgumentType::sortingMode));
        ArgumentTypeRegistry.registerArgumentType(Util.ID("bank_type"),
                BankTypeArgumentType.class, ConstantArgumentSerializer.of(BankTypeArgumentType::bankType));
    }

    private static int restoreBankCommand(CommandContext<ServerCommandSource> context) {

        ServerPlayerEntity player;
        try {
            player = EntityArgumentType.getPlayer(context, "player");
        } catch (CommandSyntaxException e) {
            return 0;
        }
        UUID uuid = UuidArgumentType.getUuid(context, "uuid");
        BankItemStorage bank = Util.getBankItemStorage(uuid,
                context.getSource().getWorld());
        if (bank == null) {
            context.getSource().sendFeedback(() -> Text.translatable("command.bankstorage.unknownid"), false);
            return 0;
        }
        ItemStack stack = Registries.ITEM
                .get(Util.ID(bank.type.getName()))
                .getDefaultStack();
        stack.set(BankStorage.UUIDComponentType, uuid);
        player.getInventory().insertStack(stack);
        return 1;
    }

    private static int listBankStorages(CommandContext<ServerCommandSource> context) {

        BankFilter filter = getFilter(context);

        List<BankItemStorage> bankItemStorages = getBankItemStorages(context)
                .stream()
                .filter(filter::matchesBank)
                .toList();

        listBankStoragesInChat(context, bankItemStorages);

        return 1;
    }

    private static int listBankStoragesSorted(CommandContext<ServerCommandSource> context) {

        BankFilter filter = getFilter(context);
        SortingMode sortingMode = SortingModeArgumentType.getSortingMode(context, "sorting_mode");

        Stream<BankItemStorage> bankItemStorages = getBankItemStorages(context)
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

    private static int listBankStoragesInChat(CommandContext<ServerCommandSource> context,
            List<BankItemStorage> bankItemStorages) {

        ServerPlayerEntity player = context.getSource().getPlayer();

        MutableText message = Text.empty();

        for (BankItemStorage bankItemStorage : bankItemStorages) {
            UUID uuid = bankItemStorage.uuid;

            long nonEmptyStacks = bankItemStorage.heldStacks.stream().filter(stack -> !stack.isEmpty()).count();

            String command = "/bankstorage fromuuid " + uuid.toString() + " " + player.getName().getString();

            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.translatable("command.bankstorage.hoverinfo"));

            // message.append(Text
            // .translatable("command.bankstorage.bankinfo", bankItemStorage.type.getName(),
            // nonEmptyStacks,
            // bankItemStorage.uuid.toString())
            // String playerName = bankItemStorage.usedByPlayerUUID;
            message.append(Text
                    .literal(bankItemStorage.type.getName() + ", " + nonEmptyStacks + " items, uuid: "
                            + bankItemStorage.uuid.toString() + ", " + bankItemStorage.usedByPlayerName
                            + "\n")
                    .styled(style -> style
                            .withClickEvent(clickEvent)
                            .withHoverEvent(hoverEvent)));
        }
        context.getSource().sendMessage(message);
        context.getSource().sendMessage(
                Text.translatable("admin_restore_bank.bankstorage.listed_banks_num", bankItemStorages.size()));

        return 1;
    }

    private static List<BankItemStorage> getBankItemStorages(CommandContext<ServerCommandSource> context) {

        MinecraftServer server = context.getSource().getServer();
        return List.copyOf(BankStateSaverAndLoader.getServerStateSaverAndLoader(server).getBankMap().values());
    }

    private static BankFilter getFilter(CommandContext<ServerCommandSource> context) {
        UUID uuid = context.getSource().getPlayer().getUuid();
        return filters.computeIfAbsent(uuid, ignoredUuid -> new BankFilter());
    }

    private static int clearFilter(CommandContext<ServerCommandSource> context) {
        getFilter(context).clear();
        context.getSource().sendMessage(Text.translatable("admin_restore_bank.bankstorage.cleared_all_filters"));
        return 1;
    }

    private static int clearFilterType(CommandContext<ServerCommandSource> context) {
        getFilter(context).clearType();
        context.getSource().sendMessage(Text.translatable("admin_restore_bank.bankstorage.cleared_type_filters"));
        return 1;
    }

    private static int clearPlayerFilter(CommandContext<ServerCommandSource> context) {
        getFilter(context).clearPlayers();
        context.getSource().sendMessage(Text.translatable("admin_restore_bank.bankstorage.cleared_player_filters"));
        return 1;
    }

    private static int clearItemFilter(CommandContext<ServerCommandSource> context) {
        getFilter(context).clearItems();
        context.getSource().sendMessage(Text.translatable("admin_restore_bank.bankstorage.cleared_item_filters"));
        return 1;
    }

    private static int addTypeFilter(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        getFilter(context).addType(BankTypeArgumentType.getBankType(context, "type"));
        context.getSource().sendMessage(Text.translatable("admin_restore_bank.bankstorage.added_type_filter"));
        return 1;
    }

    private static int addPlayerFilter(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        getFilter(context).addPlayer(EntityArgumentType.getPlayer(context, "player"));
        context.getSource().sendMessage(Text.translatable("admin_restore_bank.bankstorage.added_player_filter"));
        return 1;
    }

    private static int addItemFilter(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        getFilter(context).addItemPredicate(ItemPredicateArgumentType.getItemStackPredicate(context, "itemPredicate"));
        context.getSource().sendMessage(Text.translatable("admin_restore_bank.bankstorage.added_item_filter"));
        return 1;
    }
}
