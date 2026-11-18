package mitchellenhancedvanilla.modid.client.render;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.entity.ReinforcedArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

public class ReinforcedArrowRenderer extends ArrowRenderer<ReinforcedArrowEntity, ArrowRenderState> {
    public static final Identifier TEXTURE = MitchellEnhancedVanilla.id("textures/entity/projectiles/reinforced_arrow.png");

    public ReinforcedArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState state) {
        return TEXTURE;
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}
