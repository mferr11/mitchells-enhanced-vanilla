package mitchellenhancedvanilla.modid.client;

import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.client.render.ReinforcedArrowRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class MitchellEnhancedVanillaClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntities.REINFORCED_ARROW, ReinforcedArrowRenderer::new);
	}
}
