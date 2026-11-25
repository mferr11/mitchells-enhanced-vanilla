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
import mitchellenhancedvanilla.modid.item.KingsShieldItem;
import mitchellenhancedvanilla.modid.item.QuiverItem;
import mitchellenhancedvanilla.modid.item.TarnishedCrownItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.core.Direction;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.component.BundleContents;

public class ModItems {
	public static Item register(ResourceKey<Item> itemKey, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
		// Create the item instance.
		Item item = itemFactory.apply(settings.setId(itemKey));
		// Register the item.
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);
		// Block items link their blocks back to them (pick block, block drops), as vanilla's Items.registerItem does
		if (item instanceof BlockItem blockItem) blockItem.registerBlocks(Item.BY_BLOCK, item);
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
			creativeTab.insertAfter(Items.GOLDEN_BOOTS, ModItems.KINGS_CROWN);
			creativeTab.insertAfter(Items.SHIELD, ModItems.KINGS_SHIELD);
		});
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
		.register((creativeTab) -> {
			creativeTab.insertAfter(Items.WITHER_SKELETON_SKULL, ModItems.BARROW_KING_SKULL);
		});
        // Boss summons sit with the spawners, ahead of the eggs
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS)
		.register((creativeTab) -> {
			creativeTab.insertAfter(Items.CREAKING_HEART, ModItems.TARNISHED_CROWN);
			creativeTab.insertAfter(Items.SPIDER_SPAWN_EGG, ModItems.BROOD_SPIDER_SPAWN_EGG, ModItems.BROOD_LARVA_SPAWN_EGG);
			creativeTab.insertAfter(Items.SKELETON_SPAWN_EGG, ModItems.BARROW_SERVANT_SPAWN_EGG);
			creativeTab.insertAfter(Items.WITHER_SKELETON_SPAWN_EGG, ModItems.BARROW_KING_SPAWN_EGG);
		});
        // Each plating sits after the material it is made from
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
		.register((creativeTab) -> {
			creativeTab.insertAfter(Items.COPPER_INGOT, ModItems.COPPER_ARMOUR_PLATING);
			creativeTab.insertAfter(Items.STRING, ModItems.BROOD_SILK);
			creativeTab.insertAfter(Items.IRON_INGOT, ModItems.CHAINMAIL_ARMOUR_PLATING);
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
    public static final Item CHAINMAIL_ARMOUR_PLATING = register(ModItemIds.CHAINMAIL_ARMOUR_PLATING, Item::new, new Item.Properties());
    public static final Item COPPER_ARMOUR_PLATING = register(ModItemIds.COPPER_ARMOUR_PLATING, Item::new, new Item.Properties());
    public static final Item IRON_ARMOUR_PLATING = register(ModItemIds.IRON_ARMOUR_PLATING, Item::new, new Item.Properties());
    public static final Item GOLD_ARMOUR_PLATING = register(ModItemIds.GOLD_ARMOUR_PLATING, Item::new, new Item.Properties());
    public static final Item DIAMOND_ARMOUR_PLATING = register(ModItemIds.DIAMOND_ARMOUR_PLATING, Item::new, new Item.Properties());

    // Boss summon
    public static final Item TARNISHED_CROWN = register(ModItemIds.TARNISHED_CROWN, TarnishedCrownItem::new,
        new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));

    // Barrow King drop
    public static final Item KINGS_CROWN = register(ModItemIds.KINGS_CROWN, Item::new,
        new Item.Properties().humanoidArmor(ModArmour.KINGS_CROWN, ArmorType.HELMET).rarity(Rarity.RARE));

    // Barrow King drop: a tougher shield that bashes back melee attackers
    public static final Item KINGS_SHIELD = register(ModItemIds.KINGS_SHIELD, KingsShieldItem::new,
        KingsShieldItem.properties());

    // Barrow King trophy: placeable on floors and walls, wearable like a skeleton skull
    // Brood Spider drop; the string for bows stronger than iron
    public static final Item BROOD_SILK = register(ModItemIds.BROOD_SILK, Item::new, new Item.Properties());
    public static final Item BROOD_SPIDER_SPAWN_EGG = register(ModItemIds.BROOD_SPIDER_SPAWN_EGG, SpawnEggItem::new,
        new Item.Properties().spawnEgg(ModEntities.BROOD_SPIDER));
    public static final Item BROOD_LARVA_SPAWN_EGG = register(ModItemIds.BROOD_LARVA_SPAWN_EGG, SpawnEggItem::new,
        new Item.Properties().spawnEgg(ModEntities.BROOD_LARVA));
    public static final Item BARROW_KING_SPAWN_EGG = register(ModItemIds.BARROW_KING_SPAWN_EGG, SpawnEggItem::new,
        new Item.Properties().spawnEgg(ModEntities.BARROW_KING));
    public static final Item BARROW_SERVANT_SPAWN_EGG = register(ModItemIds.BARROW_SERVANT_SPAWN_EGG, SpawnEggItem::new,
        new Item.Properties().spawnEgg(ModEntities.BARROW_SERVANT));
    public static final Item BARROW_KING_SKULL = register(ModItemIds.BARROW_KING_SKULL,
        p -> new StandingAndWallBlockItem(ModBlocks.BARROW_KING_SKULL, ModBlocks.BARROW_KING_WALL_SKULL, Direction.DOWN, Waypoint.addHideAttribute(p)),
        new Item.Properties().rarity(Rarity.RARE).equippableUnswappable(EquipmentSlot.HEAD).useBlockDescriptionPrefix());
}
