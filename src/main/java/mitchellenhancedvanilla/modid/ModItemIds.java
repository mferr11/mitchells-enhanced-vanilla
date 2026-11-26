package mitchellenhancedvanilla.modid;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ModItemIds {
    public static ResourceKey<Item> create(String name) {
        // Create the item key
        return ResourceKey.create(Registries.ITEM, MitchellEnhancedVanilla.id(name));
    }

    public static final ResourceKey<Item> REINFORCED_ARROW = create("reinforced_arrow");
    public static final ResourceKey<Item> FROZEN_ARROW = create("frozen_arrow");
    public static final ResourceKey<Item> COPPER_ARROW = create("copper_arrow");
    public static final ResourceKey<Item> AMETHYST_ARROW = create("amethyst_arrow");
    public static final ResourceKey<Item> GOLD_ARROW = create("gold_arrow");
    public static final ResourceKey<Item> PRISMARINE_ARROW = create("prismarine_arrow");
    public static final ResourceKey<Item> EMERALD_ARROW = create("emerald_arrow");
    public static final ResourceKey<Item> IRON_BOW = create("iron_bow");
    public static final ResourceKey<Item> GOLDEN_BOW = create("golden_bow");
    public static final ResourceKey<Item> DIAMOND_BOW = create("diamond_bow");
    public static final ResourceKey<Item> NETHERITE_BOW = create("netherite_bow");
    public static final ResourceKey<Item> QUIVER = create("quiver");
    public static final ResourceKey<Item> CHAINMAIL_ARMOUR_PLATING = create("chainmail_armour_plating");
    public static final ResourceKey<Item> COPPER_ARMOUR_PLATING = create("copper_armour_plating");
    public static final ResourceKey<Item> IRON_ARMOUR_PLATING = create("iron_armour_plating");
    public static final ResourceKey<Item> GOLD_ARMOUR_PLATING = create("gold_armour_plating");
    public static final ResourceKey<Item> DIAMOND_ARMOUR_PLATING = create("diamond_armour_plating");
    public static final ResourceKey<Item> TARNISHED_CROWN = create("tarnished_crown");
    public static final ResourceKey<Item> KINGS_CROWN = create("kings_crown");
    public static final ResourceKey<Item> BARROW_KING_SKULL = create("barrow_king_skull");
    public static final ResourceKey<Item> KINGS_SHIELD = create("kings_shield");
    public static final ResourceKey<Item> BROOD_SPIDER_SPAWN_EGG = create("brood_spider_spawn_egg");
    public static final ResourceKey<Item> BROOD_LARVA_SPAWN_EGG = create("brood_larva_spawn_egg");
    public static final ResourceKey<Item> BARROW_KING_SPAWN_EGG = create("barrow_king_spawn_egg");
    public static final ResourceKey<Item> BARROW_SERVANT_SPAWN_EGG = create("barrow_servant_spawn_egg");
    public static final ResourceKey<Item> BROOD_SILK = create("brood_silk");
    public static final ResourceKey<Item> BROOD_HEART_FRAGMENT = create("brood_heart_fragment");
    public static final ResourceKey<Item> BROOD_HEART = create("brood_heart");
    public static final ResourceKey<Item> BROOD_COBWEB = create("brood_cobweb");
    public static final ResourceKey<Item> BROOD_EGG = create("brood_egg");
    public static final ResourceKey<Item> BROOD_COCOON = create("brood_cocoon");
    public static final ResourceKey<Item> BROOD_STONE = create("brood_stone");
    public static final ResourceKey<Item> POLISHED_BROOD_STONE = create("polished_brood_stone");
    public static final ResourceKey<Item> BROOD_STONE_BRICKS = create("brood_stone_bricks");
    public static final ResourceKey<Item> POLISHED_BROOD_STONE_STAIRS = create("polished_brood_stone_stairs");
    public static final ResourceKey<Item> POLISHED_BROOD_STONE_SLAB = create("polished_brood_stone_slab");
    public static final ResourceKey<Item> POLISHED_BROOD_STONE_WALL = create("polished_brood_stone_wall");
    public static final ResourceKey<Item> BROOD_STONE_BRICK_STAIRS = create("brood_stone_brick_stairs");
    public static final ResourceKey<Item> BROOD_STONE_BRICK_SLAB = create("brood_stone_brick_slab");
    public static final ResourceKey<Item> BROOD_STONE_BRICK_WALL = create("brood_stone_brick_wall");
    public static final ResourceKey<Item> BROOD_SILK_BLOCK = create("brood_silk_block");
    public static final ResourceKey<Item> BROOD_SPIDER_EYE = create("brood_spider_eye");
    public static final ResourceKey<Item> WEAVER_EYE = create("weaver_eye");
    public static final ResourceKey<Item> EMPTY_TOME = create("empty_tome");
    public static final ResourceKey<Item> TOME_OF_WEAVING = create("tome_of_weaving");
    public static final ResourceKey<Item> BARROW_TOME = create("barrow_tome");
    public static final ResourceKey<Item> BARROW_CHEST = create("barrow_chest");
    public static final ResourceKey<Item> BROOD_CHEST = create("brood_chest");
}
