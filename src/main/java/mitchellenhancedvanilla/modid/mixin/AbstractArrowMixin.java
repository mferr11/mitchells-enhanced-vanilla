package mitchellenhancedvanilla.modid.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mitchellenhancedvanilla.modid.ModParticles;
import mitchellenhancedvanilla.modid.entity.FrozenArrowEntity;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {
    // Frozen Arrows swap the crit trail for the blue version
    @ModifyExpressionValue(method = "tick", at = @At(value = "FIELD",
        target = "Lnet/minecraft/core/particles/ParticleTypes;CRIT:Lnet/minecraft/core/particles/SimpleParticleType;"))
    private SimpleParticleType mev$frozenTrail(SimpleParticleType original) {
        return (Object) this instanceof FrozenArrowEntity ? ModParticles.FROZEN_CRIT : original;
    }
}
