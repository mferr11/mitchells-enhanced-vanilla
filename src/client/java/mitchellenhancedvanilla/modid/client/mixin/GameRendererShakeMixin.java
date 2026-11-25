package mitchellenhancedvanilla.modid.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mitchellenhancedvanilla.modid.client.render.ScreenShake;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Adds camera shake on top of the hurt tilt (both the world view and the held item)
@Mixin(GameRenderer.class)
public abstract class GameRendererShakeMixin {
    // RETURN, not TAIL: bobHurt returns early whenever the player isn't hurt
    @Inject(method = "bobHurt", at = @At("RETURN"))
    private void mev$shake(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
        ScreenShake.apply(poseStack);
    }
}
