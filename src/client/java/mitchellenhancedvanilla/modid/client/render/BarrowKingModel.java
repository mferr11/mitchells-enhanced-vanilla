package mitchellenhancedvanilla.modid.client.render;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

// Skeleton model with a raised sword that clears his head and a shield held across his chest
public class BarrowKingModel extends SkeletonModel<BarrowKingRenderState> {
    // Outward tilt of the raised sword arm (radians, ~23 degrees)
    public static final float RAISED_ARM_TILT = 0.4F;
    // Summoning wave: radians per tick, then side-to-side and forward/back swing (radians)
    public static final float SUMMON_WAVE_SPEED = 0.45F;
    // Extra outward tilt while waving, so the swing never comes back over his head
    public static final float SUMMON_WAVE_TILT = 0.4F;
    public static final float SUMMON_WAVE_SIDE = 0.35F;
    public static final float SUMMON_WAVE_FORWARD = 0.25F;

    // Shield arm: raised forward (~70 degrees) and turned across the body (~55 degrees)
    public static final float SHIELD_ARM_PITCH = -1.22F;
    public static final float SHIELD_ARM_INWARD = 0.96F;
    // Shield follows his head this much when he looks up or down
    public static final float SHIELD_HEAD_FOLLOW = 0.5F;

    // Dazed: sway speed (radians per tick), head droop and side-to-side roll, arm hang and swing (radians)
    public static final float DAZED_SWAY_SPEED = 0.12F;
    public static final float DAZED_HEAD_DROOP = 0.5F;
    public static final float DAZED_HEAD_ROLL = 0.3F;
    public static final float DAZED_ARM_HANG = 0.1F;
    public static final float DAZED_ARM_SWING = 0.24F;
    // How far the limp arms hang out from his sides (radians, ~17 degrees)
    public static final float DAZED_ARM_SPLAY = 0.3F;

    public BarrowKingModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(BarrowKingRenderState state) {
        super.setupAnim(state);
        boolean rightHanded = state.mainArm == HumanoidArm.RIGHT;

        if (state.raisingSword) {
            float tilt = RAISED_ARM_TILT;
            float sweep = 0.0F;
            if (state.summoning) {
                // Waves the sword side to side and forward/back over his head
                float t = state.ageInTicks * SUMMON_WAVE_SPEED;
                tilt += SUMMON_WAVE_TILT + Mth.sin(t) * SUMMON_WAVE_SIDE;
                sweep = Mth.cos(t) * SUMMON_WAVE_FORWARD;
            }
            // Raised arm points up, so outward is -z for the right arm and +z for the left
            if (rightHanded) {
                rightArm.zRot -= tilt;
                rightArm.xRot += sweep;
            } else {
                leftArm.zRot += tilt;
                leftArm.xRot += sweep;
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

        if (state.dazed) poseDazed(state.ageInTicks);
    }

    // Woozy: head lolls in a slow circle, arms hang limp and swing a little out of step
    private void poseDazed(float age) {
        float t = age * DAZED_SWAY_SPEED;
        head.xRot = DAZED_HEAD_DROOP + Mth.sin(t * 1.3F) * 0.12F;
        head.zRot = Mth.sin(t) * DAZED_HEAD_ROLL;
        head.yRot += Mth.cos(t * 0.7F) * 0.15F;
        for (ModelPart arm : new ModelPart[] { rightArm, leftArm }) {
            float side = arm == rightArm ? 1.0F : -1.0F;
            float phase = arm == rightArm ? 0.0F : 1.7F;
            arm.xRot = DAZED_ARM_HANG + Mth.sin(t + phase) * DAZED_ARM_SWING;
            arm.yRot = 0.0F;
            // Arms hang splayed out from his sides
            arm.zRot = side * (DAZED_ARM_SPLAY + Mth.cos(t * 1.1F + phase) * 0.06F);
        }
    }
}
