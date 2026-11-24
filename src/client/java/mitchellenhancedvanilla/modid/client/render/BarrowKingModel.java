package mitchellenhancedvanilla.modid.client.render;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.world.entity.HumanoidArm;

// Skeleton model with the raised sword arm tilted out so it clears his head
public class BarrowKingModel extends SkeletonModel<BarrowKingRenderState> {
    // Outward tilt of the raised arm (radians, ~23 degrees)
    public static final float RAISED_ARM_TILT = 0.4F;

    public BarrowKingModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(BarrowKingRenderState state) {
        super.setupAnim(state);
        if (!state.raisingSword) return;
        // Raised arm points up, so outward is -z for the right arm and +z for the left
        if (state.mainArm == HumanoidArm.RIGHT) {
            rightArm.zRot -= RAISED_ARM_TILT;
        } else {
            leftArm.zRot += RAISED_ARM_TILT;
        }
    }
}
