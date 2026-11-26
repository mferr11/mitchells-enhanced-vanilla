package mitchellenhancedvanilla.modid.entity;

import mitchellenhancedvanilla.modid.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

// A ball of soul fire hurled by the King or cast from a Barrow Tome; it has no model, only particles.
// A player's cast also bursts on impact, scorching everything around it
public class SoulFlameEntity extends ThrowableProjectile {
    public static final float DAMAGE = 5.0F;
    // Fizzles out if it hits nothing (ticks)
    public static final int LIFETIME = 100;
    // Player-cast burst: reach (blocks) and damage to everything caught in it but the direct hit
    public static final double BURST_RADIUS = 3.0;
    public static final float BURST_DAMAGE = 3.0F;
    // Burst particles, like the Amethyst Arrow's shrapnel: per block of radius, how many and how fast
    public static final int BURST_PARTICLES_PER_BLOCK = 20;
    public static final double BURST_SPEED_PER_BLOCK = 0.1;

    // Already took the full hit, so the burst skips it
    private @Nullable Entity directHit;

    public SoulFlameEntity(EntityType<? extends SoulFlameEntity> type, Level level) {
        super(type, level);
    }

    public SoulFlameEntity(Level level, LivingEntity owner) {
        super(ModEntities.SOUL_FLAME, owner.getX(), owner.getEyeY() - 0.1, owner.getZ(), level);
        setOwner(owner);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    // Flies straight, like a blaze's fireball
    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            // The flame itself: a tight cluster of soul fire, plus a few wisps trailing behind
            for (int i = 0; i < 3; i++) {
                level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, getX() + (random.nextDouble() - 0.5) * 0.2,
                    getY() + (random.nextDouble() - 0.5) * 0.2, getZ() + (random.nextDouble() - 0.5) * 0.2, 0.0, 0.0, 0.0);
            }
            if (random.nextInt(3) == 0) level().addParticle(ParticleTypes.SOUL, getX(), getY(), getZ(), 0.0, 0.02, 0.0);
        } else if (tickCount > LIFETIME) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (level() instanceof ServerLevel level) {
            hit.getEntity().hurtServer(level, damageSources().thrown(this, getOwner()), DAMAGE);
            directHit = hit.getEntity();
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof ServerLevel level) {
            // Bursts into soul fire and wisps
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY(), getZ(), 12, 0.2, 0.2, 0.2, 0.05);
            level.sendParticles(ParticleTypes.SOUL, getX(), getY(), getZ(), 6, 0.2, 0.2, 0.2, 0.03);
            playSound(SoundEvents.SOUL_ESCAPE.value(), 2.0F, 0.8F);
            if (getOwner() instanceof Player) burst(level);
            discard();
        }
    }

    // Shrapnel-style spray of soul fire, and damage to every living thing in reach except the caster and the direct hit
    private void burst(ServerLevel level) {
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY(), getZ(), (int) (BURST_PARTICLES_PER_BLOCK * BURST_RADIUS),
            0.1, 0.1, 0.1, BURST_SPEED_PER_BLOCK * BURST_RADIUS);
        level.sendParticles(ParticleTypes.SOUL, getX(), getY(), getZ(), (int) (BURST_PARTICLES_PER_BLOCK * BURST_RADIUS / 3),
            0.1, 0.1, 0.1, BURST_SPEED_PER_BLOCK * BURST_RADIUS * 0.5);
        playSound(SoundEvents.BLAZE_SHOOT, 1.5F, 0.5F);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(BURST_RADIUS))) {
            if (target == getOwner() || target == directHit || target.distanceTo(this) > BURST_RADIUS) continue;
            target.hurtServer(level, damageSources().thrown(this, getOwner()), BURST_DAMAGE);
        }
    }
}
