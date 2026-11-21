package mitchellenhancedvanilla.modid.client;

import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.client.hud.BowHud;
import mitchellenhancedvanilla.modid.client.model.BowAmmoProperty;
import mitchellenhancedvanilla.modid.client.render.FrozenCritParticle;
import mitchellenhancedvanilla.modid.client.render.FrozenParticles;
import mitchellenhancedvanilla.modid.client.render.ModArrowRenderer;
import mitchellenhancedvanilla.modid.client.screen.FletchingTableScreen;
import mitchellenhancedvanilla.modid.menu.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class MitchellEnhancedVanillaClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModArrowRenderer.register(ModEntities.REINFORCED_ARROW);
		ModArrowRenderer.register(ModEntities.FROZEN_ARROW);
		BowHud.register();
		BowAmmoProperty.register();
		MenuScreens.register(ModMenus.FLETCHING_TABLE, FletchingTableScreen::new);
		FrozenParticles.register();
		FrozenCritParticle.register();
	}
}
