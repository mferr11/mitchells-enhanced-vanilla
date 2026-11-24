package mitchellenhancedvanilla.modid.entity;

import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.ModItems;
import mitchellenhancedvanilla.modid.ModParticles;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AmethystArrowEntity extends ModArrowEntity {
    // Shrapnel radius in blocks
    public static final double SHRAPNEL_RADIUS = 8.0;
    // Shrapnel damage at the centre, falling off to 0 at the edge
    public static final float SHRAPNEL_DAMAGE = 8.0F;

    // Shard particles per block of radius
    public static final int PARTICLES_PER_BLOCK = 30;
    // Particle speed per block of radius
    public static final double PARTICLE_SPEED_PER_BLOCK = 0.05;

    // Vanilla arrows have a base damage of 2.0
    public static final double BASE_DAMAGE = 3.0;

    // Vanilla removes the arrow on a mob hit, so track this separately
    private boolean shattered;

    public AmethystArrowEntity(EntityType<? extends AmethystArrowEntity> type, Level level) {
        super(type, level);
    }

    public AmethystArrowEntity(Level level, LivingEntity owner, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.AMETHYST_ARROW, level, owner, pickupItem, firedFromWeapon);
    }

    public AmethystArrowEntity(Level level, double x, double y, double z, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.AMETHYST_ARROW, level, x, y, z, pickupItem, firedFromWeapon);
    }

    @Override
    protected double baseDamage() {
        return BASE_DAMAGE;
    }

    @Override
    protected Item arrowItem() {
        return ModItems.AMETHYST_ARROW;
    }

    @Override
    public SimpleParticleType critParticle() {
        return ModParticles.AMETHYST_CRIT;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        shatter(result.getLocation(), result.getEntity());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        shatter(result.getLocation(), null);
    }

    // Burst of shrapnel, then the arrow is gone
    private void shatter(Vec3 center, @Nullable Entity directHit) {
        if (shattered || !(level() instanceof ServerLevel level)) return;
        shattered = true;

        Entity owner = getOwner();
        DamageSource source = damageSources().arrow(this, owner != null ? owner : this);
        AABB area = new AABB(center, center).inflate(SHRAPNEL_RADIUS);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            // Direct hit already took arrow damage; never hurt the shooter
            if (target == directHit || target == owner || !target.isAlive()) continue;
            Vec3 targetCenter = target.getBoundingBox().getCenter();
            double distance = targetCenter.distanceTo(center);
            if (distance > SHRAPNEL_RADIUS || !hasLineOfSight(level, center, targetCenter)) continue;

            float damage = (float) (SHRAPNEL_DAMAGE * (1.0 - distance / SHRAPNEL_RADIUS));
            if (damage > 0.5F) target.hurtServer(level, source, damage);
        }

        // Burst size scales with the radius
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.AMETHYST_SHARD),
            center.x, center.y, center.z, (int) (PARTICLES_PER_BLOCK * SHRAPNEL_RADIUS),
            0.1, 0.1, 0.1, PARTICLE_SPEED_PER_BLOCK * SHRAPNEL_RADIUS);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.NEUTRAL, 1.5F, 1.0F);
        discard();
    }

    // Walls block shrapnel
    private boolean hasLineOfSight(ServerLevel level, Vec3 from, Vec3 to) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this))
            .getType() == HitResult.Type.MISS;
    }
}
