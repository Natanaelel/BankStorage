package net.natte.bankstorage.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class BankSerializer {

    private static final Codec<ItemStack> LARGE_STACK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
                    Codec.INT.fieldOf("count").forGetter(ItemStack::getCount),
                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
            ).apply(instance, ItemStack::new)
    );

    private static final Codec<ItemStack> OPTIONAL_LARGE_STACK_CODEC = airIfError(optionalOrEmptyMap(LARGE_STACK_CODEC));
    private static final Codec<ItemStack> OPTIONAL_SINGLE_ITEM_CODEC = airIfError(optionalOrEmptyMap(ItemStack.SINGLE_ITEM_CODEC));

    private static final Codec<BankItemStorage> BANK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.STRING_CODEC
                    .fieldOf("uuid")
                    .forGetter(BankItemStorage::uuid),
            BankType.CODEC
                    .fieldOf("type")
                    .forGetter(BankItemStorage::type),
            OPTIONAL_LARGE_STACK_CODEC.listOf()
                    .fieldOf("items")
                    .forGetter(BankItemStorage::getItems),
            // maps must have string keys
            Codec.unboundedMap(Codec.STRING.xmap(Integer::valueOf, String::valueOf), OPTIONAL_SINGLE_ITEM_CODEC)
                    .fieldOf("locked_slots")
                    .forGetter(BankItemStorage::getLockedSlots),
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


    private static Codec<ItemStack> optionalOrEmptyMap(Codec<ItemStack> codec) {
        return ExtraCodecs.optionalEmptyMap(codec)
                .xmap(
                        maybeStack -> maybeStack.orElse(ItemStack.EMPTY),
                        stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
    }

    private static Codec<ItemStack> airIfError(Codec<ItemStack> codec) {
        return Codec.withAlternative(codec, Codec.unit(ItemStack.EMPTY));
    }
}
