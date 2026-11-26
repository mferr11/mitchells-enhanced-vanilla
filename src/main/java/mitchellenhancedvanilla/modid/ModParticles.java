package mitchellenhancedvanilla.modid;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;

public class ModParticles {
    // Coloured crit trails behind fully charged arrows (colours set in the client initializer)
    public static final SimpleParticleType FROZEN_CRIT = register("frozen_crit");
    public static final SimpleParticleType COPPER_CRIT = register("copper_crit");
    public static final SimpleParticleType AMETHYST_CRIT = register("amethyst_crit");
    public static final SimpleParticleType GOLD_CRIT = register("gold_crit");
    public static final SimpleParticleType PRISMARINE_CRIT = register("prismarine_crit");
    public static final SimpleParticleType EMERALD_CRIT = register("emerald_crit");

    // The Barrow King's sword arc, in soul-fire blue
    public static final SimpleParticleType SOUL_SWEEP = register("soul_sweep");

    // Barrow Tome casts (trail and burst): soul fire and wisps that vanish quickly (lifetimes set in the client initializer)
    public static final SimpleParticleType SHORT_SOUL_FLAME = register("short_soul_flame");
    public static final SimpleParticleType SHORT_SOUL_WISP = register("short_soul_wisp");

    // Thrown Eye of Weaving's trail: the Eye of Ender's portal particle in brood colours
    public static final SimpleParticleType BROOD_PORTAL = register("brood_portal");

    private static SimpleParticleType register(String name) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, MitchellEnhancedVanilla.id(name), FabricParticleTypes.simple());
    }

    public static void initialize() {
    }
}
