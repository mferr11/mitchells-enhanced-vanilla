package mitchellenhancedvanilla.modid.client.render;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.ARGB;

public final class FrozenTint {
    // Multiplied with the model's normal colour
    public static final int TINT = ARGB.opaque(0x4781FF);

    private FrozenTint() {}

    public static int apply(int color, LivingEntityRenderState state) {
        return state.isFullyFrozen ? ARGB.multiply(color, TINT) : color;
    }
}
