package mitchellenhancedvanilla.modid.client.render;

import mitchellenhancedvanilla.modid.entity.BarrowKingEntity;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;

public class BarrowKingRenderState extends SkeletonRenderState {
    // His synced combat stance (named to avoid the vanilla entity "pose" field)
    public BarrowKingEntity.Pose kingPose = BarrowKingEntity.Pose.GUARD;
    // Shield up in front of him (also depends on him still holding it)
    public boolean shieldRaised;
}
