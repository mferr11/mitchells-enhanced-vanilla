package mitchellenhancedvanilla.modid;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;

public class ModTags {
    // Items allowed in each fletching table slot
    public static final TagKey<Item> FLETCHING_TIPS = TagKey.create(Registries.ITEM, MitchellEnhancedVanilla.id("fletching_tips"));
    public static final TagKey<Item> FLETCHING_SHAFTS = TagKey.create(Registries.ITEM, MitchellEnhancedVanilla.id("fletching_shafts"));
    public static final TagKey<Item> FLETCHING_FLETCHINGS = TagKey.create(Registries.ITEM, MitchellEnhancedVanilla.id("fletching_fletchings"));

    // Biomes where the Tarnished Crown can summon the Barrow King
    public static final TagKey<Biome> BARROW_KING_BIOMES = TagKey.create(Registries.BIOME, MitchellEnhancedVanilla.id("barrow_king_summonable"));
}
