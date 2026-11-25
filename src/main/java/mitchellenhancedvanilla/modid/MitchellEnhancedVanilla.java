package mitchellenhancedvanilla.modid;

import mitchellenhancedvanilla.modid.menu.FletchingRecipesPayload;
import mitchellenhancedvanilla.modid.menu.FletchingTableInteraction;
import mitchellenhancedvanilla.modid.menu.ModMenus;
import mitchellenhancedvanilla.modid.quiver.CycleQuiverPayload;
import mitchellenhancedvanilla.modid.quiver.Quiver;
import mitchellenhancedvanilla.modid.recipe.ModRecipes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MitchellEnhancedVanilla implements ModInitializer {
	public static final String MOD_ID = "mitchells-enhanced-vanilla";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
		ModBlocks.initialize();
		ModItems.initialize();
		ModEntities.initialize();
		BroodSpawning.initialize();
		ModRecipes.initialize();
		ModMenus.initialize();
		ModParticles.initialize();
		ModLootTables.register();
		FletchingTableInteraction.register();
		Quiver.initialize();
		CycleQuiverPayload.register();
		ModArmour.register();
		FletchingRecipesPayload.register();
		ScreenShakePayload.register();
		DefaultItemComponentEvents.MODIFY.register(ctx -> ctx.modify
			(Items.BOW, builder -> builder.set(DataComponents.MAX_DAMAGE, BowStats.WOODEN_DURABILITY)));
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
