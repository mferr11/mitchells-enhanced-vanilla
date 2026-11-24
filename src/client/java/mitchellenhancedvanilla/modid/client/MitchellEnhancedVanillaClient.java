package mitchellenhancedvanilla.modid.client;

import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.client.hud.ArmourHotbar;
import mitchellenhancedvanilla.modid.client.hud.BowHud;
import mitchellenhancedvanilla.modid.client.hud.QuiverHotbar;
import mitchellenhancedvanilla.modid.client.model.BowAmmoProperty;
import mitchellenhancedvanilla.modid.ModParticles;
import mitchellenhancedvanilla.modid.client.render.BarrowKingRenderer;
import mitchellenhancedvanilla.modid.client.render.BarrowRetainerRenderer;
import mitchellenhancedvanilla.modid.client.render.ColoredCritParticle;
import mitchellenhancedvanilla.modid.client.render.FrozenParticles;
import mitchellenhancedvanilla.modid.client.render.ModArrowRenderer;
import mitchellenhancedvanilla.modid.client.screen.FletchingRecipePanel;
import mitchellenhancedvanilla.modid.client.screen.FletchingTableScreen;
import mitchellenhancedvanilla.modid.menu.ModMenus;
import mitchellenhancedvanilla.modid.item.QuiverItem;
import mitchellenhancedvanilla.modid.quiver.QuiverCapacity;
import mitchellenhancedvanilla.modid.quiver.QuiverTooltip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class MitchellEnhancedVanillaClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModArrowRenderer.register(ModEntities.REINFORCED_ARROW);
		ModArrowRenderer.register(ModEntities.FROZEN_ARROW);
		ModArrowRenderer.register(ModEntities.COPPER_ARROW);
		ModArrowRenderer.register(ModEntities.AMETHYST_ARROW);
		ModArrowRenderer.register(ModEntities.GOLD_ARROW);
		ModArrowRenderer.register(ModEntities.PRISMARINE_ARROW);
		ModArrowRenderer.register(ModEntities.EMERALD_ARROW);
		EntityRenderers.register(ModEntities.BARROW_KING, BarrowKingRenderer::new);
		EntityRenderers.register(ModEntities.BARROW_RETAINER, BarrowRetainerRenderer::new);
		BowHud.register();
		ArmourHotbar.register();
		QuiverHotbar.register();
		QuiverKeys.register();
		ClientTooltipComponentCallback.EVENT.register(data -> {
			if (!(data instanceof QuiverTooltip quiver)) return null;
			ClientBundleTooltip tooltip = new ClientBundleTooltip(quiver.contents());
			((QuiverCapacity) tooltip).mev$setCapacity(QuiverItem.CAPACITY);
			return tooltip;
		});
		BowAmmoProperty.register();
		MenuScreens.register(ModMenus.FLETCHING_TABLE, FletchingTableScreen::new);
		FletchingRecipePanel.register();
		FrozenParticles.register();
		ArrowTooltip.register();
		BowTooltip.register();
		// Arrow crit trail colours
		ColoredCritParticle.register(ModParticles.FROZEN_CRIT, 0x66B2FF);
		ColoredCritParticle.register(ModParticles.COPPER_CRIT, 0xFF8A2E);
		ColoredCritParticle.register(ModParticles.AMETHYST_CRIT, 0xB38EF3);
		ColoredCritParticle.register(ModParticles.GOLD_CRIT, 0xF9D93B);
		ColoredCritParticle.register(ModParticles.PRISMARINE_CRIT, 0x72D8C4);
		ColoredCritParticle.register(ModParticles.EMERALD_CRIT, 0x17DD62);
	}
}
