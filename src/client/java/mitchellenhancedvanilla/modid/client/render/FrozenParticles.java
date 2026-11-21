package mitchellenhancedvanilla.modid.client.render;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

// Snowflakes around fully frozen entities
public final class FrozenParticles {
    // Chance per tick, per entity
    private static final float SPAWN_CHANCE = 0.4F;
    // Max sideways speed
    private static final double DRIFT = 0.03;

    private FrozenParticles() {}

    public static void register() {
        ClientTickEvents.END_LEVEL_TICK.register(FrozenParticles::tick);
    }

    private static void tick(ClientLevel level) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused()) return;
        RandomSource random = level.getRandom();

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !living.isFullyFrozen() || living.isInvisible()) continue;
            // Not on yourself in first person
            if (living == mc.player && mc.options.getCameraType().isFirstPerson()) continue;
            if (random.nextFloat() >= SPAWN_CHANCE) continue;

            level.addParticle(ParticleTypes.SNOWFLAKE,
                living.getRandomX(0.6), living.getRandomY(), living.getRandomZ(0.6),
                (random.nextDouble() - 0.5) * 2 * DRIFT, 0.0, (random.nextDouble() - 0.5) * 2 * DRIFT);
        }
    }
}
