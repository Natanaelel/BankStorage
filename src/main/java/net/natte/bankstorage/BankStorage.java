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
import net.minecraft.component.DataComponentType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Uuids;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankStorage implements ModInitializer {

    public static final String MOD_ID = "bankstorage";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final BankType BANK_1 = new BankType("bank_1", 256, 1, 9, 176, 114 + 18 * 1);
    private static final BankType BANK_2 = new BankType("bank_2", 1024, 2, 9, 176, 114 + 18 * 2);
    private static final BankType BANK_3 = new BankType("bank_3", 4096, 3, 9, 176, 114 + 18 * 3);
    private static final BankType BANK_4 = new BankType("bank_4", 16_384, 4, 9, 176, 114 + 18 * 4);
    private static final BankType BANK_5 = new BankType("bank_5", 65_536, 6, 9, 176, 114 + 18 * 6);
    private static final BankType BANK_6 = new BankType("bank_6", 262_144, 9, 9, 176, 114 + 18 * 9);
    private static final BankType BANK_7 = new BankType("bank_7", 1_000_000_000, 12, 9, 176, 114 + 18 * 12);

    public static final LinkItem LINK_ITEM = new LinkItem(new Item.Settings().maxCount(1));

    public static final List<BankType> bankTypes = new ArrayList<>();

    public static final Block BANK_DOCK_BLOCK = new BankDockBlock(AbstractBlock.Settings.create().strength(5.0f, 6.0f)
            .mapColor(MapColor.BLACK).requiresTool().sounds(BlockSoundGroup.METAL).nonOpaque());

    public static BlockEntityType<BankDockBlockEntity> BANK_DOCK_BLOCK_ENTITY;

    public static final DataComponentType<UUID> UUIDComponentType = DataComponentType.<UUID>builder().codec(Uuids.CODEC).packetCodec(Uuids.PACKET_CODEC).build();
    public static final DataComponentType<BankOptions> OptionsComponentType = DataComponentType.<BankOptions>builder().codec(BankOptions.CODEC).packetCodec(BankOptions.PACKET_CODEC).build();
    public static final DataComponentType<BankType> BankTypeComponentType = DataComponentType.<BankType>builder().codec(BankType.CODEC).packetCodec(BankType.PACKET_CODEC).build();

    @Override
    public void onInitialize() {

        registerBanks();
        registerDock();
        registerLink();
        registerRecipes();
        registerCommands();
        registerPackets();
        registerNetworkListeners();

        registerEventListeners();
        registerItemComponentTypes();

    }

    private void registerItemComponentTypes() {
        Registry.register(Registries.DATA_COMPONENT_TYPE, Util.ID("uuid"), UUIDComponentType);
        Registry.register(Registries.DATA_COMPONENT_TYPE, Util.ID("options"), OptionsComponentType);
        Registry.register(Registries.DATA_COMPONENT_TYPE, Util.ID("type"), BankTypeComponentType);

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
        Registry.register(Registries.ITEM, Util.ID("bank_link"), LINK_ITEM);
    }

    private void registerBanks() {

        BANK_1.register(bankTypes);
        BANK_2.register(bankTypes);
        BANK_3.register(bankTypes);
        BANK_4.register(bankTypes);
        BANK_5.register(bankTypes);
        BANK_6.register(bankTypes);
        BANK_7.register(bankTypes, new Item.Settings().fireproof());

        CauldronBehavior.WATER_CAULDRON_BEHAVIOR.map().put(LINK_ITEM, CauldronBehavior.CLEAN_DYEABLE_ITEM);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(group -> {
            bankTypes.forEach(type -> {
                group.add(type.item);
            });
            group.add(LINK_ITEM);
            group.add(BANK_DOCK_BLOCK);
        });
    }

    private void registerDock() {

        Registry.register(Registries.BLOCK, Util.ID("bank_dock"), BANK_DOCK_BLOCK);
        Registry.register(Registries.ITEM, Util.ID("bank_dock"),
                new BlockItem(BANK_DOCK_BLOCK, new Item.Settings()));

        BANK_DOCK_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Util.ID("bank_dock_block_entity"),
                BlockEntityType.Builder.create(BankDockBlockEntity::new, BANK_DOCK_BLOCK).build());

        ItemStorage.SIDED.registerForBlockEntity(
                (bankDockBlockEntity, direction) -> bankDockBlockEntity.getItemStorage(), BANK_DOCK_BLOCK_ENTITY);

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

    private void registerPackets() {

        PayloadTypeRegistry.playS2C().register(ItemStackBobbingAnimationPacketS2C.PACKET_ID, ItemStackBobbingAnimationPacketS2C.PACKET_CODEC);
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
