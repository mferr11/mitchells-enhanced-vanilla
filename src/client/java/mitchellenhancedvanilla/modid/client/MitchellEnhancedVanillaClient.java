package mitchellenhancedvanilla.modid.client;

import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.client.hud.BowHud;
import mitchellenhancedvanilla.modid.client.model.BowAmmoProperty;
import mitchellenhancedvanilla.modid.client.render.FrozenArrowRenderer;
import mitchellenhancedvanilla.modid.client.render.ReinforcedArrowRenderer;
import mitchellenhancedvanilla.modid.client.screen.FletchingTableScreen;
import mitchellenhancedvanilla.modid.menu.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class MitchellEnhancedVanillaClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntities.REINFORCED_ARROW, ReinforcedArrowRenderer::new);
		EntityRenderers.register(ModEntities.FROZEN_ARROW, FrozenArrowRenderer::new);
		BowHud.register();
		BowAmmoProperty.register();
		MenuScreens.register(ModMenus.FLETCHING_TABLE, FletchingTableScreen::new);
	}
}
