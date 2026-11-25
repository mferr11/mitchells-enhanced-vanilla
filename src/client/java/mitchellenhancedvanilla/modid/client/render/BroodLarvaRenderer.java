package mitchellenhancedvanilla.modid.client.render;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.entity.BroodLarvaEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.endermite.EndermiteModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

// Vanilla endermite model with the brood larva texture
public class BroodLarvaRenderer extends MobRenderer<BroodLarvaEntity, LivingEntityRenderState, EndermiteModel> {
    private static final Identifier TEXTURE = MitchellEnhancedVanilla.id("textures/entity/brood_larvae.png");

    public BroodLarvaRenderer(EntityRendererProvider.Context context) {
        super(context, new EndermiteModel(context.bakeLayer(ModelLayers.ENDERMITE)), 0.3F);
    }

    // Dies on its back like an endermite
    @Override
    protected float getFlipDegrees() {
        return 180.0F;
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}
