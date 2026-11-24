package mitchellenhancedvanilla.modid.client.render;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

// Skeleton model with a raised sword that clears his head and a shield held across his chest
public class BarrowKingModel extends SkeletonModel<BarrowKingRenderState> {
    // Outward tilt of the raised sword arm (radians, ~23 degrees)
    public static final float RAISED_ARM_TILT = 0.4F;

    // Shield arm: raised forward (~70 degrees) and turned across the body (~55 degrees)
    public static final float SHIELD_ARM_PITCH = -1.22F;
    public static final float SHIELD_ARM_INWARD = 0.96F;
    // Shield follows his head this much when he looks up or down
    public static final float SHIELD_HEAD_FOLLOW = 0.5F;

    public BarrowKingModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(BarrowKingRenderState state) {
        super.setupAnim(state);
        boolean rightHanded = state.mainArm == HumanoidArm.RIGHT;

        if (state.raisingSword) {
            // Raised arm points up, so outward is -z for the right arm and +z for the left
            if (rightHanded) {
                rightArm.zRot -= RAISED_ARM_TILT;
            } else {
                leftArm.zRot += RAISED_ARM_TILT;
            }
        }

        if (state.shieldRaised) {
            ModelPart shieldArm = rightHanded ? leftArm : rightArm;
            // Inward is +y for the left arm and -y for the right
            float inward = rightHanded ? SHIELD_ARM_INWARD : -SHIELD_ARM_INWARD;
            shieldArm.xRot = SHIELD_ARM_PITCH + Mth.clamp(head.xRot, -0.6F, 0.4F) * SHIELD_HEAD_FOLLOW;
            shieldArm.yRot = inward;
            shieldArm.zRot = 0.0F;
        }
    }
}
