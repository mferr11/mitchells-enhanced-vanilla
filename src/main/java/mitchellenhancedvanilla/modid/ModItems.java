package mitchellenhancedvanilla.modid;

import java.util.function.Function;

import mitchellenhancedvanilla.modid.entity.AmethystArrowEntity;
import mitchellenhancedvanilla.modid.entity.CopperArrowEntity;
import mitchellenhancedvanilla.modid.entity.EmeraldArrowEntity;
import mitchellenhancedvanilla.modid.entity.FrozenArrowEntity;
import mitchellenhancedvanilla.modid.entity.GoldArrowEntity;
import mitchellenhancedvanilla.modid.entity.PrismarineArrowEntity;
import mitchellenhancedvanilla.modid.entity.ReinforcedArrowEntity;
import mitchellenhancedvanilla.modid.item.ModArrowItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ModItems {
	public static Item register(ResourceKey<Item> itemKey, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
		// Create the item instance.
		Item item = itemFactory.apply(settings.setId(itemKey));
		// Register the item.
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);
		return item;
	}
    
    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
		.register((creativeTab) -> {
			creativeTab.accept(ModItems.IRON_BOW);
			creativeTab.accept(ModItems.GOLDEN_BOW);
			creativeTab.accept(ModItems.DIAMOND_BOW);
			creativeTab.accept(ModItems.NETHERITE_BOW);
			creativeTab.accept(ModItems.REINFORCED_ARROW);
			creativeTab.accept(ModItems.FROZEN_ARROW);
			creativeTab.accept(ModItems.COPPER_ARROW);
			creativeTab.accept(ModItems.AMETHYST_ARROW);
			creativeTab.accept(ModItems.GOLD_ARROW);
			creativeTab.accept(ModItems.PRISMARINE_ARROW);
			creativeTab.accept(ModItems.EMERALD_ARROW);
		});
    }

    public static final Item REINFORCED_ARROW = register(ModItemIds.REINFORCED_ARROW,
        p -> new ModArrowItem(p, ReinforcedArrowEntity.BASE_DAMAGE, ReinforcedArrowEntity::new, ReinforcedArrowEntity::new), new Item.Properties());
    public static final Item FROZEN_ARROW = register(ModItemIds.FROZEN_ARROW,
        p -> new ModArrowItem(p, FrozenArrowEntity.BASE_DAMAGE, FrozenArrowEntity::new, FrozenArrowEntity::new), new Item.Properties());
    public static final Item COPPER_ARROW = register(ModItemIds.COPPER_ARROW,
        p -> new ModArrowItem(p, CopperArrowEntity.BASE_DAMAGE, CopperArrowEntity::new, CopperArrowEntity::new), new Item.Properties());
    public static final Item AMETHYST_ARROW = register(ModItemIds.AMETHYST_ARROW,
        p -> new ModArrowItem(p, AmethystArrowEntity.BASE_DAMAGE, AmethystArrowEntity::new, AmethystArrowEntity::new), new Item.Properties());
    public static final Item GOLD_ARROW = register(ModItemIds.GOLD_ARROW,
        p -> new ModArrowItem(p, GoldArrowEntity.BASE_DAMAGE, GoldArrowEntity::new, GoldArrowEntity::new), new Item.Properties());
    public static final Item PRISMARINE_ARROW = register(ModItemIds.PRISMARINE_ARROW,
        p -> new ModArrowItem(p, PrismarineArrowEntity.BASE_DAMAGE, PrismarineArrowEntity::new, PrismarineArrowEntity::new), new Item.Properties());
    public static final Item EMERALD_ARROW = register(ModItemIds.EMERALD_ARROW,
        p -> new ModArrowItem(p, EmeraldArrowEntity.BASE_DAMAGE, EmeraldArrowEntity::new, EmeraldArrowEntity::new), new Item.Properties());

    // Enchantability matches the material's tools (vanilla bow is 1)
    public static final Item IRON_BOW = register(ModItemIds.IRON_BOW, BowItem::new,
        new Item.Properties().durability(BowStats.IRON_DURABILITY).enchantable(14).repairable(Items.IRON_INGOT));
    public static final Item GOLDEN_BOW = register(ModItemIds.GOLDEN_BOW, BowItem::new,
        new Item.Properties().durability(BowStats.GOLDEN_DURABILITY).enchantable(22).repairable(Items.GOLD_INGOT));
    public static final Item DIAMOND_BOW = register(ModItemIds.DIAMOND_BOW, BowItem::new,
        new Item.Properties().durability(BowStats.DIAMOND_DURABILITY).enchantable(10).repairable(Items.DIAMOND));
    public static final Item NETHERITE_BOW = register(ModItemIds.NETHERITE_BOW, BowItem::new,
        new Item.Properties().durability(BowStats.NETHERITE_DURABILITY).enchantable(15).repairable(Items.NETHERITE_INGOT).fireResistant());
}
