package mitchellenhancedvanilla.modid.client.render;

import mitchellenhancedvanilla.modid.ModParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.particle.CritParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;

// Vanilla crit particle, recoloured blue
public final class FrozenCritParticle {
    private static final int COLOR = 0x66B2FF;

    private FrozenCritParticle() {}

    public static void register() {
        ParticleProviderRegistry.getInstance().register(ModParticles.FROZEN_CRIT, sprites -> {
            CritParticle.Provider crit = new CritParticle.Provider(sprites);
            return (ParticleProvider<SimpleParticleType>) (type, level, x, y, z, dx, dy, dz, random) -> {
                Particle particle = crit.createParticle(type, level, x, y, z, dx, dy, dz, random);
                if (particle instanceof SingleQuadParticle quad) {
                    // Slight brightness variation, like vanilla crits
                    float brightness = 0.75F + random.nextFloat() * 0.25F;
                    quad.setColor(
                        ((COLOR >> 16) & 0xFF) / 255F * brightness,
                        ((COLOR >> 8) & 0xFF) / 255F * brightness,
                        (COLOR & 0xFF) / 255F * brightness);
                }
                return particle;
            };
        });
    }
}
