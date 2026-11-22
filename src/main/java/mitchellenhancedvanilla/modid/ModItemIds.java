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
}
