package mitchellenhancedvanilla.modid.client.mixin;

import mitchellenhancedvanilla.modid.block.entity.BroodHeartBlockEntity;
import mitchellenhancedvanilla.modid.client.render.BroodHeartPulse;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Counts each time the Brood Heart texture loops back to its first frame (the start of the swell)
@Mixin(SpriteContents.AnimationState.class)
public abstract class SpriteAnimationStateMixin {
    @Shadow
    private int frame;
    @Shadow
    private int subFrame;

    @Inject(method = "tick", at = @At("TAIL"))
    private void mev$countHeartBeat(CallbackInfo ci) {
        if ((Object) this == BroodHeartPulse.animation && frame == 0 && subFrame == 0) BroodHeartBlockEntity.clientBeats++;
    }
}
