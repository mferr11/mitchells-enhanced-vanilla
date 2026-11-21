package mitchellenhancedvanilla.modid;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;

public class ModParticles {
    // Blue crit trail behind fully charged Frozen Arrows
    public static final SimpleParticleType FROZEN_CRIT = Registry.register(
        BuiltInRegistries.PARTICLE_TYPE, MitchellEnhancedVanilla.id("frozen_crit"), FabricParticleTypes.simple());

    public static void initialize() {
    }
}
