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
import mitchellenhancedvanilla.modid.item.QuiverItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;

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
			creativeTab.insertAfter(Items.BOW,
				ModItems.IRON_BOW, ModItems.GOLDEN_BOW, ModItems.DIAMOND_BOW, ModItems.NETHERITE_BOW);
			creativeTab.insertBefore(Items.ARROW, ModItems.QUIVER);
			creativeTab.insertAfter(Items.ARROW,
				ModItems.REINFORCED_ARROW, ModItems.FROZEN_ARROW, ModItems.COPPER_ARROW, ModItems.AMETHYST_ARROW,
				ModItems.GOLD_ARROW, ModItems.PRISMARINE_ARROW, ModItems.EMERALD_ARROW);
		});
        // Each plating sits after the material it is made from
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
		.register((creativeTab) -> {
			creativeTab.insertAfter(Items.COPPER_INGOT, ModItems.COPPER_ARMOUR_PLATING);
			creativeTab.insertAfter(Items.IRON_INGOT, ModItems.IRON_ARMOUR_PLATING);
			creativeTab.insertAfter(Items.GOLD_INGOT, ModItems.GOLD_ARMOUR_PLATING);
			creativeTab.insertAfter(Items.DIAMOND, ModItems.DIAMOND_ARMOUR_PLATING);
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

    public static final Item QUIVER = register(ModItemIds.QUIVER, QuiverItem::new,
        new Item.Properties().stacksTo(1).component(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY));

    // Crafted from a 2x2 of the material, used in place of it in armour recipes
    public static final Item COPPER_ARMOUR_PLATING = register(ModItemIds.COPPER_ARMOUR_PLATING, Item::new, new Item.Properties());
    public static final Item IRON_ARMOUR_PLATING = register(ModItemIds.IRON_ARMOUR_PLATING, Item::new, new Item.Properties());
    public static final Item GOLD_ARMOUR_PLATING = register(ModItemIds.GOLD_ARMOUR_PLATING, Item::new, new Item.Properties());
    public static final Item DIAMOND_ARMOUR_PLATING = register(ModItemIds.DIAMOND_ARMOUR_PLATING, Item::new, new Item.Properties());
}
