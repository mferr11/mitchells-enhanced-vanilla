package mitchellenhancedvanilla.modid.client.render;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.entity.BarrowServantEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;

// Vanilla skeleton model with the Barrow King's minion texture
public class BarrowServantRenderer extends AbstractSkeletonRenderer<BarrowServantEntity, SkeletonRenderState> {
    private static final Identifier TEXTURE = MitchellEnhancedVanilla.id("textures/entity/barrow_king/barrow_king_minion.png");

    public BarrowServantRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.SKELETON, ModelLayers.SKELETON_ARMOR);
    }

    @Override
    public Identifier getTextureLocation(SkeletonRenderState state) {
        return TEXTURE;
    }

    @Override
    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }
}
