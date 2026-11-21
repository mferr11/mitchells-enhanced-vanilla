package mitchellenhancedvanilla.modid.client;

import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.client.hud.BowHud;
import mitchellenhancedvanilla.modid.client.model.BowAmmoProperty;
import mitchellenhancedvanilla.modid.ModParticles;
import mitchellenhancedvanilla.modid.client.render.ColoredCritParticle;
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
		ModArrowRenderer.register(ModEntities.COPPER_ARROW);
		ModArrowRenderer.register(ModEntities.AMETHYST_ARROW);
		ModArrowRenderer.register(ModEntities.GOLD_ARROW);
		BowHud.register();
		BowAmmoProperty.register();
		MenuScreens.register(ModMenus.FLETCHING_TABLE, FletchingTableScreen::new);
		FrozenParticles.register();
		ArrowTooltip.register();
		// Arrow crit trail colours
		ColoredCritParticle.register(ModParticles.FROZEN_CRIT, 0x66B2FF);
		ColoredCritParticle.register(ModParticles.COPPER_CRIT, 0xFF8A2E);
		ColoredCritParticle.register(ModParticles.AMETHYST_CRIT, 0xB38EF3);
		ColoredCritParticle.register(ModParticles.GOLD_CRIT, 0xF9D93B);
	}
}
