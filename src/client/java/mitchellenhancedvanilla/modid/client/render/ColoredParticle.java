package mitchellenhancedvanilla.modid.client.render;

import java.util.function.Function;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.particle.AttackSweepParticle;
import net.minecraft.client.particle.CritParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;

// Vanilla particles recoloured: the vanilla provider builds the particle, then it's tinted
public final class ColoredParticle {
    private ColoredParticle() {}

    // Arrow crit trail; color is 0xRRGGBB
    public static void crit(SimpleParticleType type, int color) {
        register(type, color, CritParticle.Provider::new, 0.75F);
    }

    // Sword sweep arc; color is 0xRRGGBB
    public static void sweep(SimpleParticleType type, int color) {
        register(type, color, AttackSweepParticle.Provider::new, 0.8F);
    }

    // Eye of Ender style portal swirl; color is 0xRRGGBB
    public static void portal(SimpleParticleType type, int color) {
        register(type, color, PortalParticle.Provider::new, 0.5F);
    }

    // Each particle gets a slight random brightness between minBrightness and 1, like vanilla's own variation
    private static void register(SimpleParticleType type, int color,
                                 Function<SpriteSet, ParticleProvider<SimpleParticleType>> vanilla, float minBrightness) {
        float r = ARGB.redFloat(color);
        float g = ARGB.greenFloat(color);
        float b = ARGB.blueFloat(color);
        ParticleProviderRegistry.getInstance().register(type, sprites -> {
            ParticleProvider<SimpleParticleType> base = vanilla.apply(sprites);
            return (t, level, x, y, z, dx, dy, dz, random) -> {
                Particle particle = base.createParticle(t, level, x, y, z, dx, dy, dz, random);
                if (particle instanceof SingleQuadParticle quad) {
                    float brightness = minBrightness + random.nextFloat() * (1.0F - minBrightness);
                    quad.setColor(r * brightness, g * brightness, b * brightness);
                }
                return particle;
            };
        });
    }
}
