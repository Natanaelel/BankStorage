package net.natte.bankstorage.state;

import java.time.LocalDateTime;
import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;

import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;

public class BankSerializer {

    private static final Codec<BankItemStorage> BANK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.STRING_CODEC
                    .fieldOf("uuid")
                    .forGetter(b -> b.uuid),
            BankType.CODEC
                    .fieldOf("type")
                    .forGetter(b -> b.type),
            ItemStack.OPTIONAL_CODEC.listOf()
                    .fieldOf("items")
                    .forGetter(b -> b.getItems()),
            // maps must have string keys
            Codec.unboundedMap(Codec.STRING.xmap(Integer::valueOf, String::valueOf), ItemStack.OPTIONAL_CODEC)
                    .fieldOf("locked_slots")
                    .forGetter(b -> b.getlockedSlots()),
            Codec.STRING
                    .lenientOptionalFieldOf("date_created", LocalDateTime.now().toString())
                    .forGetter(b -> b.dateCreated.toString()),
            UUIDUtil.STRING_CODEC
                    .lenientOptionalFieldOf("last_used_by_uuid", BankStorage.FAKE_PLAYER_UUID)
                    .forGetter(b -> b.usedByPlayerUUID),
            ExtraCodecs.PLAYER_NAME
                    .lenientOptionalFieldOf("last_used_by_player", "World")
                    .forGetter(b -> b.usedByPlayerName)

    ).apply(instance, BankItemStorage::createFromCodec));

    public static final Codec<List<BankItemStorage>> CODEC = BANK_CODEC.listOf();
}
