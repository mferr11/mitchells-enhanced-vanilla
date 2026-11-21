package mitchellenhancedvanilla.modid;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.UniformGenerator;

public final class ModLootTables {
    // Gold Arrows from piglin bartering; same weight (40 of 469) and count as spectral arrows
    public static final int GOLD_ARROW_WEIGHT = 40;
    public static final int GOLD_ARROW_MIN = 6;
    public static final int GOLD_ARROW_MAX = 12;

    private ModLootTables() {}

    public static void register() {
        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            // Only vanilla's table, so data packs that replace it are left alone
            if (!source.isBuiltin() || !key.equals(BuiltInLootTables.PIGLIN_BARTERING)) return;

            Holder<ContextIntProvider> count = Holder.direct(new UniformGenerator(
                Holder.<ContextIntProvider>direct(new ConstantValue(GOLD_ARROW_MIN)),
                Holder.<ContextIntProvider>direct(new ConstantValue(GOLD_ARROW_MAX))));
            Holder<LootItemFunction> setCount = Holder.direct(SetItemCountFunction.setCount(count).build());

            // Added to the existing pool so a barter still gives one thing
            builder.modifyPools(pool -> pool.add(
                LootItem.lootTableItem(ModItems.GOLD_ARROW).setWeight(GOLD_ARROW_WEIGHT).apply(setCount)));
        });
    }
}
