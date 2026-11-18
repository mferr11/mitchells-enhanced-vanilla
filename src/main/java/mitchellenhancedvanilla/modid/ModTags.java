package mitchellenhancedvanilla.modid;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    /** Items allowed in each fletching table slot. Defined in data/mitchells-enhanced-vanilla/tags/item/. */
    public static final TagKey<Item> FLETCHING_TIPS = TagKey.create(Registries.ITEM, MitchellEnhancedVanilla.id("fletching_tips"));
    public static final TagKey<Item> FLETCHING_SHAFTS = TagKey.create(Registries.ITEM, MitchellEnhancedVanilla.id("fletching_shafts"));
    public static final TagKey<Item> FLETCHING_FLETCHINGS = TagKey.create(Registries.ITEM, MitchellEnhancedVanilla.id("fletching_fletchings"));
}
