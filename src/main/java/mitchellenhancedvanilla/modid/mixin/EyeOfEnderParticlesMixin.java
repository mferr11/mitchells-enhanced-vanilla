package mitchellenhancedvanilla.modid.mixin;

import mitchellenhancedvanilla.modid.ModItems;
import mitchellenhancedvanilla.modid.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EyeOfEnder.class)
public abstract class EyeOfEnderParticlesMixin {
    // A thrown Eye of Weaving trails brood-coloured particles instead of the Eye of Ender's purple ones
    @ModifyArg(method = "spawnParticles", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), index = 0)
    private ParticleOptions mev$broodTrail(ParticleOptions particle) {
        if (particle == ParticleTypes.PORTAL && ((EyeOfEnder) (Object) this).getItem().is(ModItems.WEAVER_EYE)) {
            return ModParticles.BROOD_PORTAL;
        }
        return particle;
    }
}
