package mitchellenhancedvanilla.modid.menu;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModMenus {
    public static final MenuType<FletchingTableMenu> FLETCHING_TABLE = Registry.register(
        BuiltInRegistries.MENU,
        MitchellEnhancedVanilla.id("fletching_table"),
        new MenuType<>(FletchingTableMenu::new, FeatureFlags.VANILLA_SET));

    public static void initialize() {
        // Loads this class so the menu types above get registered.
    }
}
