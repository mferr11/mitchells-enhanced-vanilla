package mitchellenhancedvanilla.modid.client.render;

import java.util.function.Function;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.particle.EmissiveRisingParticle;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

// Vanilla particles with a much shorter life, for quick bursts (vanilla flames live 12 to 44 ticks)
public final class ShortLivedParticle {
    private ShortLivedParticle() {}

    // Soul fire, living min to max ticks
    public static void flame(SimpleParticleType type, int min, int max) {
        register(type, FlameParticle.Provider::new, min, max);
    }

    // Soul wisp, living min to max ticks (its animation speeds up to fit)
    public static void wisp(SimpleParticleType type, int min, int max) {
        register(type, EmissiveRisingParticle.Provider::new, min, max);
    }

    private static void register(SimpleParticleType type, Function<SpriteSet, ParticleProvider<SimpleParticleType>> vanilla, int min, int max) {
        ParticleProviderRegistry.getInstance().register(type, sprites -> {
            ParticleProvider<SimpleParticleType> base = vanilla.apply(sprites);
            return (t, level, x, y, z, dx, dy, dz, random) -> {
                Particle particle = base.createParticle(t, level, x, y, z, dx, dy, dz, random);
                if (particle != null) particle.setLifetime(min + random.nextInt(max - min + 1));
                return particle;
            };
        });
    }
}
