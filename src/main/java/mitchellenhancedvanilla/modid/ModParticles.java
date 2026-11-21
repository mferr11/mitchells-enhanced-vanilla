package mitchellenhancedvanilla.modid;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;

public class ModParticles {
    // Coloured crit trails behind fully charged arrows (colours set in the client initializer)
    public static final SimpleParticleType FROZEN_CRIT = register("frozen_crit");
    public static final SimpleParticleType COPPER_CRIT = register("copper_crit");

    private static SimpleParticleType register(String name) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, MitchellEnhancedVanilla.id(name), FabricParticleTypes.simple());
    }

    public static void initialize() {
    }
}
