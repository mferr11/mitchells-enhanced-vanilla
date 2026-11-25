package mitchellenhancedvanilla.modid.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.entity.BroodSpiderEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

// Vanilla spider model scaled up, with the brood texture and its own glowing eyes
public class BroodSpiderRenderer extends MobRenderer<BroodSpiderEntity, LivingEntityRenderState, SpiderModel> {
    private static final Identifier TEXTURE = MitchellEnhancedVanilla.id("textures/entity/spider/brood_spider.png");
    private static final RenderType EYES = RenderTypes.eyes(MitchellEnhancedVanilla.id("textures/entity/spider/brood_spider_eyes.png"));

    public BroodSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel(context.bakeLayer(ModelLayers.SPIDER)), 0.8F * BroodSpiderEntity.SCALE);
        addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return EYES;
            }
        });
    }

    @Override
    protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
        poseStack.scale(BroodSpiderEntity.SCALE, BroodSpiderEntity.SCALE, BroodSpiderEntity.SCALE);
    }

    // Dies on its back like a vanilla spider
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
