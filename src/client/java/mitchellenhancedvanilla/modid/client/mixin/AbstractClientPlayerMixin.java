package mitchellenhancedvanilla.modid.client.mixin;

import mitchellenhancedvanilla.modid.BowStats;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    // Bow zoom-in reaches its max at full draw. Vanilla divides by 20
    @ModifyConstant(method = "getFieldOfViewModifier", constant = @Constant(floatValue = 20.0F))
    private float mev$bowZoomDrawTime(float original) {
        return BowStats.DRAW_TICKS;
    }
}
