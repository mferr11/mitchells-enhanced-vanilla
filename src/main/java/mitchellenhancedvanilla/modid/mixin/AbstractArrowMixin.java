package mitchellenhancedvanilla.modid.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mitchellenhancedvanilla.modid.entity.ModArrowEntity;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {
    // Mod arrows can swap the crit trail particle
    @ModifyExpressionValue(method = "tick", at = @At(value = "FIELD",
        target = "Lnet/minecraft/core/particles/ParticleTypes;CRIT:Lnet/minecraft/core/particles/SimpleParticleType;"))
    private SimpleParticleType mev$critTrail(SimpleParticleType original) {
        return (Object) this instanceof ModArrowEntity arrow ? arrow.critParticle() : original;
    }
}
