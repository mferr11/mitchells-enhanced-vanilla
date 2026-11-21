package mitchellenhancedvanilla.modid.mixin;

import mitchellenhancedvanilla.modid.DecoyTarget;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCubeMob.class)
public abstract class CubeMobDecoyMixin {
    // Distracted slimes don't hurt on contact
    @Inject(method = "canDealDamage", at = @At("HEAD"), cancellable = true)
    private void mev$harmlessWhileDistracted(CallbackInfoReturnable<Boolean> cir) {
        if (((DecoyTarget) this).mev$isDistracted()) cir.setReturnValue(false);
    }
}
