package mitchellenhancedvanilla.modid.client.render;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.particle.CritParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;

// Vanilla crit particle, recoloured
public final class ColoredCritParticle {
    private ColoredCritParticle() {}

    // color is 0xRRGGBB
    public static void register(SimpleParticleType type, int color) {
        float r = ((color >> 16) & 0xFF) / 255F;
        float g = ((color >> 8) & 0xFF) / 255F;
        float b = (color & 0xFF) / 255F;
        ParticleProviderRegistry.getInstance().register(type, sprites -> {
            CritParticle.Provider crit = new CritParticle.Provider(sprites);
            return (ParticleProvider<SimpleParticleType>) (t, level, x, y, z, dx, dy, dz, random) -> {
                Particle particle = crit.createParticle(t, level, x, y, z, dx, dy, dz, random);
                if (particle instanceof SingleQuadParticle quad) {
                    // Slight brightness variation, like vanilla crits
                    float brightness = 0.75F + random.nextFloat() * 0.25F;
                    quad.setColor(r * brightness, g * brightness, b * brightness);
                }
                return particle;
            };
        });
    }
}
