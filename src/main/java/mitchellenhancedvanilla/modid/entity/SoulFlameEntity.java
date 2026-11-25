package mitchellenhancedvanilla.modid.entity;

import mitchellenhancedvanilla.modid.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

// A ball of soul fire the King hurls at players he can't reach; it has no model, only particles
public class SoulFlameEntity extends ThrowableProjectile {
    public static final float DAMAGE = 7.0F;
    // Fizzles out if it hits nothing (ticks)
    public static final int LIFETIME = 100;

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
            discard();
        }
    }
}
