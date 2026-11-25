package mitchellenhancedvanilla.modid.client.render;

import net.minecraft.client.renderer.entity.state.SkeletonRenderState;

public class BarrowKingRenderState extends SkeletonRenderState {
    // Sword raised overhead for a wind-up
    public boolean raisingSword;
    // Waving the raised sword to call his servants
    public boolean summoning;
    // Shield up in front of him
    public boolean shieldRaised;
    // Reeling from a stun: drooping head, limp arms
    public boolean dazed;
}
