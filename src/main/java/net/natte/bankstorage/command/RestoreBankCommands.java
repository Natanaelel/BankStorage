package net.natte.bankstorage.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.world.BankStateSaverAndLoader;

public class RestoreBankCommands {

    private static Map<UUID, BankFilter> filters = new HashMap<>();

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("bankstorage")
                    .requires(context -> context.hasPermissionLevel(2))
                    .then(literal("list").executes(RestoreBankCommands::listBankStorages))
                    .then(literal("filter")
                            .then(literal("clear")
                                    .executes(RestoreBankCommands::clearFilter)
                                    .then(literal("type")
                                            .executes(RestoreBankCommands::clearFilterType))
                                    .then(literal("player")
                                            .executes(RestoreBankCommands::clearPlayerFilter))
                                    .then(literal("items")
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

    private static int listBankStorages(CommandContext<ServerCommandSource> context) {

        BankFilter filter = getFilter(context);

        MutableText message = Text.empty();
        MinecraftServer server = context.getSource().getServer();
        ServerPlayerEntity player = context.getSource().getPlayer();

        Map<UUID, BankItemStorage> bankMap = BankStateSaverAndLoader.getServerStateSaverAndLoader(server).getBankMap();

        for (Map.Entry<UUID, BankItemStorage> entry : bankMap.entrySet()) {
            UUID uuid = entry.getKey();
            BankItemStorage bankItemStorage = entry.getValue();
            if (!filter.matchesBank(bankItemStorage))
                continue;

            long nonEmptyStacks = bankItemStorage.stacks.stream().filter(stack -> !stack.isEmpty()).count();

            String command = "/bankstorage fromuuid " + uuid.toString() + " " + player.getEntityName();

            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.translatable("command.bankstorage.hoverinfo"));

            message.append(Text
                    .translatable("command.bankstorage.bankinfo", bankItemStorage.type.getName(), nonEmptyStacks,
                            bankItemStorage.uuid.toString())
                    .styled(style -> style
                            .withClickEvent(clickEvent)
                            .withHoverEvent(hoverEvent)));
        }
        context.getSource().sendMessage(message);

        return 1;
    }

    private static BankFilter getFilter(CommandContext<ServerCommandSource> context) {
        UUID uuid = context.getSource().getPlayer().getUuid();
        return filters.computeIfAbsent(uuid, ignoredUuid -> new BankFilter());
    }

    private static int clearFilter(CommandContext<ServerCommandSource> context) {
        getFilter(context).clear();
        return 1;
    }

    private static int clearFilterType(CommandContext<ServerCommandSource> context) {
        getFilter(context).clearType();
        return 1;
    }

    private static int clearPlayerFilter(CommandContext<ServerCommandSource> context) {
        getFilter(context).clearPlayers();
        return 1;
    }

    private static int clearItemFilter(CommandContext<ServerCommandSource> context) {
        getFilter(context).clearItems();
        return 1;
    }

    private static int addTypeFilter(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        getFilter(context).addType(BankTypeArgumentType.getBankType(context, "type"));
        return 1;
    }

    private static int addPlayerFilter(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        getFilter(context).addPlayer(EntityArgumentType.getPlayer(context, "player"));
        return 1;
    }

    private static int addItemFilter(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        getFilter(context).addItemPredicate(ItemPredicateArgumentType.getItemStackPredicate(context, "itemPredicate"));
        return 1;
    }
}
