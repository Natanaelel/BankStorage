package net.natte.bankstorage;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.Minecraft;
import net.minecraft.component.DataComponentType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.natte.bankstorage.access.SyncedRandomAccess;
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
// import net.natte.bankstorage.packet.screensync.SyncContainerPacketS2C;
// import net.natte.bankstorage.packet.screensync.SyncLargeSlotPacketS2C;
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
import net.natte.bankstorage.util.Util;

import java.util.Random;
import java.util.UUID;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
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


    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<MenuType<?>> SCREEN_HANDLERS = DeferredRegister.create(Registries.MENU, MOD_ID);


    public BankStorage(IEventBus modBus) {

        registerBanks();
        registerDock();
        registerLink();
        registerRecipes();
        registerCommands();
        registerPackets();
        registerNetworkListeners();

        registerEventListeners();
        registerItemComponentTypes();

        ITEMS.register(modBus);
        DATA_COMPONENTS.register(modBus);
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        SCREEN_HANDLERS.register(modBus);

        modBus.addListener(this::addItemsToCreativeTab);
        modBus.addListener(this::registerCapabilities);
    }

    private void registerItemComponentTypes() {
        DATA_COMPONENTS.register("uuid", id -> UUIDComponentType);
        DATA_COMPONENTS.register("options", id -> OptionsComponentType);
        DATA_COMPONENTS.register("type", id -> BankTypeComponentType);
    }


    private void registerEventListeners() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            long randomSeed = handler.player.getRandom().nextLong();
            ((SyncedRandomAccess) handler.player).bankstorage$setSyncedRandom(new Random(randomSeed));
            sender.sendPacket(new SyncedRandomPacketS2C(randomSeed));
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            Random random = ((SyncedRandomAccess) oldPlayer).bankstorage$getSyncedRandom();
            ((SyncedRandomAccess) newPlayer).bankstorage$setSyncedRandom(random);
        });

    }

    private void registerLink() {
        BankStorage.ITEMS.register("bank_link", id -> BANK_LINK);
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
        BLOCKS.register("bank_dock", id -> BANK_DOCK_BLOCK);
        ITEMS.register("bank_dock", id -> new BlockItem(BANK_DOCK_BLOCK, new Item.Properties()));

        BLOCK_ENTITIES.register("bank_dock_block_entity", id -> BANK_DOCK_BLOCK_ENTITY);

        ItemStorage.SIDED.registerForBlockEntity(
                (bankDockBlockEntity, direction) -> bankDockBlockEntity.getItemStorage(), BANK_DOCK_BLOCK_ENTITY);

    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BANK_DOCK_BLOCK_ENTITY, (dock, side) -> new InvWrapper(dock.getItemStorage()));
    }

    private void registerRecipes() {
        Registry.register(Registries.RECIPE_SERIALIZER, Util.ID("bank_upgrade"),
                new BankRecipe.Serializer());
        Registry.register(Registries.RECIPE_SERIALIZER, Util.ID("bank_link"),
                new BankLinkRecipe.Serializer());

    }

    public void registerCommands() {
        RestoreBankCommands.register();

    }

    private void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(ItemStackBobbingAnimationPacketS2C.PACKET_ID, ItemStackBobbingAnimationPacketS2C.STREAM_CODEC, ItemStackBobbingAnimationPacketS2C::handle);
        registrar.playToClient(RequestBankStoragePacketS2C.PACKET_ID, RequestBankStoragePacketS2C.STREAM_CODEC, RequestBankStoragePacketS2C::handle);

        PayloadTypeRegistry.playS2C().register(RequestBankStoragePacketS2C.PACKET_ID, RequestBankStoragePacketS2C.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncedRandomPacketS2C.PACKET_ID, SyncedRandomPacketS2C.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(LockedSlotsPacketS2C.PACKET_ID, LockedSlotsPacketS2C.PACKET_CODEC);

        PayloadTypeRegistry.playC2S().register(UpdateBankOptionsPacketC2S.PACKET_ID, UpdateBankOptionsPacketC2S.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(OpenBankFromKeyBindPacketC2S.PACKET_ID, OpenBankFromKeyBindPacketC2S.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(RequestBankStoragePacketC2S.PACKET_ID, RequestBankStoragePacketC2S.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(SortPacketC2S.PACKET_ID, SortPacketC2S.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(PickupModePacketC2S.PACKET_ID, PickupModePacketC2S.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(SelectedSlotPacketC2S.PACKET_ID, SelectedSlotPacketC2S.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(LockSlotPacketC2S.PACKET_ID, LockSlotPacketC2S.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(KeyBindUpdatePacketC2S.PACKET_ID, KeyBindUpdatePacketC2S.PACKET_CODEC);

    }

    public void registerNetworkListeners() {

        ServerPlayNetworking.registerGlobalReceiver(RequestBankStoragePacketC2S.PACKET_ID, new RequestBankStoragePacketC2S.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(SortPacketC2S.PACKET_ID, new SortPacketC2S.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(PickupModePacketC2S.PACKET_ID, new PickupModePacketC2S.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(SelectedSlotPacketC2S.PACKET_ID, new SelectedSlotPacketC2S.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(LockSlotPacketC2S.PACKET_ID, new LockSlotPacketC2S.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(KeyBindUpdatePacketC2S.PACKET_ID, new KeyBindUpdatePacketC2S.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(UpdateBankOptionsPacketC2S.PACKET_ID, new UpdateBankOptionsPacketC2S.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(OpenBankFromKeyBindPacketC2S.PACKET_ID, new OpenBankFromKeyBindPacketC2S.Receiver());

    }
}
