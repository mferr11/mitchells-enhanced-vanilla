package mitchellenhancedvanilla.modid;

import java.util.function.Function;

import mitchellenhancedvanilla.modid.entity.CopperArrowEntity;
import mitchellenhancedvanilla.modid.entity.FrozenArrowEntity;
import mitchellenhancedvanilla.modid.entity.ReinforcedArrowEntity;
import mitchellenhancedvanilla.modid.item.ModArrowItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

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
			creativeTab.accept(ModItems.REINFORCED_ARROW);
			creativeTab.accept(ModItems.FROZEN_ARROW);
			creativeTab.accept(ModItems.COPPER_ARROW);
		});
    }

    public static final Item REINFORCED_ARROW = register(ModItemIds.REINFORCED_ARROW,
        p -> new ModArrowItem(p, ReinforcedArrowEntity::new, ReinforcedArrowEntity::new), new Item.Properties());
    public static final Item FROZEN_ARROW = register(ModItemIds.FROZEN_ARROW,
        p -> new ModArrowItem(p, FrozenArrowEntity::new, FrozenArrowEntity::new), new Item.Properties());
    public static final Item COPPER_ARROW = register(ModItemIds.COPPER_ARROW,
        p -> new ModArrowItem(p, CopperArrowEntity::new, CopperArrowEntity::new), new Item.Properties());
}
