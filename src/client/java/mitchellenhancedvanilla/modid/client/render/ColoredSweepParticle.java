package mitchellenhancedvanilla.modid.client.render;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.particle.AttackSweepParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;

// Vanilla sword sweep arc, recoloured
public final class ColoredSweepParticle {
    private ColoredSweepParticle() {}

    // color is 0xRRGGBB
    public static void register(SimpleParticleType type, int color) {
        float r = ((color >> 16) & 0xFF) / 255F;
        float g = ((color >> 8) & 0xFF) / 255F;
        float b = (color & 0xFF) / 255F;
        ParticleProviderRegistry.getInstance().register(type, sprites -> {
            AttackSweepParticle.Provider sweep = new AttackSweepParticle.Provider(sprites);
            return (ParticleProvider<SimpleParticleType>) (t, level, x, y, z, dx, dy, dz, random) -> {
                Particle particle = sweep.createParticle(t, level, x, y, z, dx, dy, dz, random);
                if (particle instanceof SingleQuadParticle quad) {
                    // Vanilla picks a random grey; keep a little of that variation in the new colour
                    float brightness = 0.8F + random.nextFloat() * 0.2F;
                    quad.setColor(r * brightness, g * brightness, b * brightness);
                }
                return particle;
            };
        });
    }
}
