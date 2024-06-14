package net.natte.bankstorage;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.natte.bankstorage.block.BankDockBlock;
import net.natte.bankstorage.blockentity.BankDockBlockEntity;
import net.natte.bankstorage.command.RestoreBankCommands;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.item.LinkItem;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.packet.client.ItemStackBobbingAnimationPacketS2C;
import net.natte.bankstorage.packet.client.RequestBankStoragePacketS2C;
import net.natte.bankstorage.packet.client.SyncedRandomPacketS2C;
import net.natte.bankstorage.packet.screensync.LockedSlotsPacketS2C;
import net.natte.bankstorage.packet.server.KeyBindUpdatePacketC2S;
import net.natte.bankstorage.packet.server.LockSlotPacketC2S;
import net.natte.bankstorage.packet.server.OpenBankFromKeyBindPacketC2S;
import net.natte.bankstorage.packet.server.PickupModePacketC2S;
import net.natte.bankstorage.packet.server.RequestBankStoragePacketC2S;
import net.natte.bankstorage.packet.server.SelectedSlotPacketC2S;
import net.natte.bankstorage.packet.server.SortPacketC2S;
import net.natte.bankstorage.packet.server.UpdateBankOptionsPacketC2S;
import net.natte.bankstorage.recipe.BankLinkRecipe;
import net.natte.bankstorage.recipe.BankRecipe;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.screen.BankScreenHandlerFactory;
import net.natte.bankstorage.state.BankStateManager;
import net.natte.bankstorage.util.Util;

import java.util.Random;
import java.util.UUID;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(BankStorage.MOD_ID)
public class BankStorage {

    public static final String MOD_ID = "bankstorage";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final UUID FAKE_PLAYER_UUID = UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77");

    private static final BankType BANK_1 = new BankType("bank_1", 256, 1, 9, 176, 114 + 18);
    private static final BankType BANK_2 = new BankType("bank_2", 1024, 2, 9, 176, 114 + 18 * 2);
    private static final BankType BANK_3 = new BankType("bank_3", 4096, 3, 9, 176, 114 + 18 * 3);
    private static final BankType BANK_4 = new BankType("bank_4", 16_384, 4, 9, 176, 114 + 18 * 4);
    private static final BankType BANK_5 = new BankType("bank_5", 65_536, 6, 9, 176, 114 + 18 * 6);
    private static final BankType BANK_6 = new BankType("bank_6", 262_144, 9, 9, 176, 114 + 18 * 9);
    private static final BankType BANK_7 = new BankType("bank_7", 1_000_000_000, 12, 9, 176, 114 + 18 * 12);

    public static final BankType[] BANK_TYPES = {BANK_1, BANK_2, BANK_3, BANK_4, BANK_5, BANK_6, BANK_7};

    public static final LinkItem BANK_LINK = new LinkItem(new Item.Properties().stacksTo(1));

    public static final Block BANK_DOCK_BLOCK = new BankDockBlock(BlockBehaviour.Properties.of().strength(5.0f, 6.0f)
            .mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion());

    public static final BlockEntityType<BankDockBlockEntity> BANK_DOCK_BLOCK_ENTITY = BlockEntityType.Builder.of(BankDockBlockEntity::new, BANK_DOCK_BLOCK).build(null);

    public static final DataComponentType<UUID> UUIDComponentType = DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build();
    public static final DataComponentType<BankOptions> OptionsComponentType = DataComponentType.<BankOptions>builder().persistent(BankOptions.CODEC).networkSynchronized(BankOptions.STREAM_CODEC).build();
    public static final DataComponentType<BankType> BankTypeComponentType = DataComponentType.<BankType>builder().persistent(BankType.CODEC).networkSynchronized(BankType.STREAM_CODEC).build();

    public static final MenuType<BankScreenHandler> MENU_TYPE = IMenuTypeExtension.create(BankScreenHandlerFactory::createClientScreenHandler);
    public static final AttachmentType<Random> SYNCED_RANDOM_ATTACHMENT = AttachmentType.builder(() -> new Random()).build();


    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<MenuType<?>> SCREEN_HANDLERS = DeferredRegister.create(Registries.MENU, MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, BankStorage.MOD_ID);


    public BankStorage(IEventBus modBus) {

        registerBanks();
        registerDock();
        registerLink();
        registerRecipes();
        registerCommands(modBus);
        registerScreenHandlers();

        registerPlayerSyncedRandom(modBus);
        registerItemComponentTypes();

        ITEMS.register(modBus);
        DATA_COMPONENTS.register(modBus);
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        SCREEN_HANDLERS.register(modBus);
        ATTACHMENT_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        COMMAND_ARGUMENT_TYPES.register(modBus);

        modBus.addListener(this::addItemsToCreativeTab);
        modBus.addListener(this::registerCapabilities);
        modBus.addListener(this::registerPackets);
        modBus.addListener(ServerStartedEvent.class, BankStateManager::initialize);
        modBus.addListener(EntityEvent.EntityConstructing.class, entityConstructing -> {
            if ((entityConstructing.getEntity() instanceof ItemEntity itemEntity) && Util.isBankLike(itemEntity.getItem()))
                itemEntity.setUnlimitedLifetime();
        });
    }

    private void registerScreenHandlers() {
        SCREEN_HANDLERS.register("bank_menu", () -> MENU_TYPE);

    }

    private void registerItemComponentTypes() {
        DATA_COMPONENTS.register("uuid", () -> UUIDComponentType);
        DATA_COMPONENTS.register("options", () -> OptionsComponentType);
        DATA_COMPONENTS.register("type", () -> BankTypeComponentType);
    }


    private void registerPlayerSyncedRandom(IEventBus modBus) {
        ATTACHMENT_TYPES.register("random", () -> SYNCED_RANDOM_ATTACHMENT);

        // create and send random (seed) on join
        modBus.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                long randomSeed = player.getRandom().nextLong();
                Random random = new Random(randomSeed);
                player.setData(SYNCED_RANDOM_ATTACHMENT, random);
                player.connection.send(new SyncedRandomPacketS2C(randomSeed));
            }
        });

        // copy random on clone
        modBus.addListener(PlayerEvent.Clone.class, event -> {
            event.getEntity().setData(SYNCED_RANDOM_ATTACHMENT, event.getOriginal().getData(SYNCED_RANDOM_ATTACHMENT));
        });
    }

    private void registerLink() {
        BankStorage.ITEMS.register("bank_link", () -> BANK_LINK);
        CauldronInteraction.WATER.map().put(BANK_LINK, CauldronInteraction.DYED_ITEM);
    }

    private void registerBanks() {

        BANK_1.register();
        BANK_2.register();
        BANK_3.register();
        BANK_4.register();
        BANK_5.register();
        BANK_6.register();
        BANK_7.register(new Item.Properties().fireResistant());
    }

    private void addItemsToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.FUNCTIONAL_BLOCKS)
            return;

        event.accept(BANK_1.item);
        event.accept(BANK_2.item);
        event.accept(BANK_3.item);
        event.accept(BANK_4.item);
        event.accept(BANK_5.item);
        event.accept(BANK_6.item);
        event.accept(BANK_7.item);
        event.accept(BANK_LINK);
        event.accept(BANK_DOCK_BLOCK);

    }

    private void registerDock() {
        BLOCKS.register("bank_dock", () -> BANK_DOCK_BLOCK);
        ITEMS.register("bank_dock", () -> new BlockItem(BANK_DOCK_BLOCK, new Item.Properties()));

        BLOCK_ENTITIES.register("bank_dock_block_entity", () -> BANK_DOCK_BLOCK_ENTITY);


    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BANK_DOCK_BLOCK_ENTITY, (dock, side) -> dock.getItemHandler());
    }

    private void registerRecipes() {
        RECIPE_SERIALIZERS.register("bank_update", BankRecipe.Serializer::new);
        RECIPE_SERIALIZERS.register("bank_link", BankLinkRecipe.Serializer::new);
//        Registry.register(Registries.RECIPE_SERIALIZER, Util.ID("bank_upgrade"),
//                new BankRecipe.Serializer());
//        Registry.register(Registries.RECIPE_SERIALIZER, Util.ID("bank_link"),
//                new BankLinkRecipe.Serializer());
    }

    public void registerCommands(IEventBus modBus) {
        RestoreBankCommands.registerArgumentTypes();
        modBus.addListener(RestoreBankCommands::registerCommands);
    }

    private void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID);

        registrar.playToClient(ItemStackBobbingAnimationPacketS2C.TYPE, ItemStackBobbingAnimationPacketS2C.STREAM_CODEC, ItemStackBobbingAnimationPacketS2C::handle);
        registrar.playToClient(RequestBankStoragePacketS2C.TYPE, RequestBankStoragePacketS2C.STREAM_CODEC, RequestBankStoragePacketS2C::handle);
        registrar.playToClient(SyncedRandomPacketS2C.TYPE, SyncedRandomPacketS2C.STREAM_CODEC, SyncedRandomPacketS2C::handle);
        registrar.playToClient(LockedSlotsPacketS2C.TYPE, LockedSlotsPacketS2C.STREAM_CODEC, LockedSlotsPacketS2C::handle);

        registrar.playToServer(UpdateBankOptionsPacketC2S.TYPE, UpdateBankOptionsPacketC2S.STREAM_CODEC, UpdateBankOptionsPacketC2S::handle);
        registrar.playToServer(OpenBankFromKeyBindPacketC2S.TYPE, OpenBankFromKeyBindPacketC2S.STREAM_CODEC, OpenBankFromKeyBindPacketC2S::handle);
        registrar.playToServer(RequestBankStoragePacketC2S.TYPE, RequestBankStoragePacketC2S.STREAM_CODEC, RequestBankStoragePacketC2S::handle);
        registrar.playToServer(SortPacketC2S.TYPE, SortPacketC2S.STREAM_CODEC, SortPacketC2S::handle);
        registrar.playToServer(PickupModePacketC2S.TYPE, PickupModePacketC2S.STREAM_CODEC, PickupModePacketC2S::handle);
        registrar.playToServer(SelectedSlotPacketC2S.TYPE, SelectedSlotPacketC2S.STREAM_CODEC, SelectedSlotPacketC2S::handle);
        registrar.playToServer(LockSlotPacketC2S.TYPE, LockSlotPacketC2S.STREAM_CODEC, LockSlotPacketC2S::handle);
        registrar.playToServer(KeyBindUpdatePacketC2S.TYPE, KeyBindUpdatePacketC2S.STREAM_CODEC, KeyBindUpdatePacketC2S::handle);

    }
}
