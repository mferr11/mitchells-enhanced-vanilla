package mitchellenhancedvanilla.modid.client.render;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.entity.BarrowKingEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;

// Wither-skeleton-sized skeleton; the pose is driven by his synced combat stance
public class BarrowKingRenderer extends HumanoidMobRenderer<BarrowKingEntity, BarrowKingRenderState, BarrowKingModel> {
    private static final Identifier TEXTURE = MitchellEnhancedVanilla.id("textures/entity/barrow_king/barrow_king.png");

    public BarrowKingRenderer(EntityRendererProvider.Context context) {
        super(context, new BarrowKingModel(context.bakeLayer(ModelLayers.WITHER_SKELETON)), 0.6F);
        // Armour uses the same model so sleeves follow the tilted arm
        addLayer(new HumanoidArmorLayer<>(this,
            ArmorModelSet.bake(ModelLayers.WITHER_SKELETON_ARMOR, context.getModelSet(), BarrowKingModel::new),
            context.getEquipmentRenderer()));
    }

    @Override
    public void extractRenderState(BarrowKingEntity king, BarrowKingRenderState state, float partialTicks) {
        super.extractRenderState(king, state, partialTicks);
        state.kingPose = king.getKingPose();
        state.shieldRaised = king.isShieldRaised();
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(BarrowKingEntity king, HumanoidArm arm) {
        if (arm == king.getMainArm()) {
            return king.getKingPose().swordRaised() ? HumanoidModel.ArmPose.THROW_TRIDENT : HumanoidModel.ArmPose.ITEM;
        }
        return king.isShieldRaised() ? HumanoidModel.ArmPose.BLOCK : HumanoidModel.ArmPose.ITEM;
    }

    @Override
    public Identifier getTextureLocation(BarrowKingRenderState state) {
        return TEXTURE;
    }

    @Override
    public BarrowKingRenderState createRenderState() {
        return new BarrowKingRenderState();
    }
}
