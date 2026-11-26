package mitchellenhancedvanilla.modid.client.mixin;

import mitchellenhancedvanilla.modid.client.render.BroodHeartPulse;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Remembers the Brood Heart texture's animation, so its loop can be heard
@Mixin(SpriteContents.class)
public abstract class SpriteContentsMixin {
    @Inject(method = "createAnimationState", at = @At("RETURN"))
    private void mev$trackHeartAnimation(CallbackInfoReturnable<SpriteContents.AnimationState> cir) {
        SpriteContents self = (SpriteContents) (Object) this;
        if (cir.getReturnValue() != null && self.name().equals(BroodHeartPulse.SPRITE)) BroodHeartPulse.animation = cir.getReturnValue();
    }
}
