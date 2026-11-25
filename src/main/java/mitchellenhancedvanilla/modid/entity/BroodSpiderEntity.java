package mitchellenhancedvanilla.modid.entity;

import mitchellenhancedvanilla.modid.ModBlocks;
import mitchellenhancedvanilla.modid.ModEntities;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;

// A bigger, tougher spider that spits webs to pin players in place from range
public class BroodSpiderEntity extends Spider {
    // Vanilla spider: 16 health, 2 damage, 0.3 speed
    public static final double MAX_HEALTH = 40.0;
    public static final double ATTACK_DAMAGE = 4.0;
    public static final double ARMOR = 2.0;
    public static final double MOVEMENT_SPEED = 0.25;
    // Renders and collides at this multiple of a vanilla spider
    public static final float SCALE = 1.4F;

    // Spits at targets between these distances (blocks); closer ones get bitten instead
    public static final double SPIT_MIN_RANGE = 4.0;
    public static final double SPIT_MAX_RANGE = 15.0;
    // Time between spits: minimum plus a random extra (ticks)
    public static final int SPIT_COOLDOWN_MIN = 80;
    public static final int SPIT_COOLDOWN_SPREAD = 40;
    // Launch speed and spread (snow golem uses 1.6 and 12)
    public static final float SPIT_SPEED = 1.3F;
    public static final float SPIT_INACCURACY = 4.0F;
    // Aims at this fraction of the target's height (the web lands at their feet anyway)
    public static final double SPIT_AIM_HEIGHT = 0.3;
    // Telegraph: stands still, hisses and drips web for this long before spitting (ticks)
    public static final int SPIT_WINDUP_TICKS = 15;

    // On death: chance its brood bursts out, and how many larvae
    public static final float LARVA_CHANCE = 0.75F;
    public static final int LARVAE_MIN = 2;
    public static final int LARVAE_MAX = 3;

    private int spitCooldown = SPIT_COOLDOWN_MIN;
    // Counts down while winding up a spit; 0 when not
    private int spitWindup;

    public BroodSpiderEntity(EntityType<? extends BroodSpiderEntity> type, Level level) {
        super(type, level);
        xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes()
            .add(Attributes.MAX_HEALTH, MAX_HEALTH)
            .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
            .add(Attributes.ARMOR, ARMOR)
            .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED);
    }

    // Vanilla spider's goals, minus the daylight checks (it hunts day or night) and the fear of armadillos
    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel level && random.nextFloat() < LARVA_CHANCE) releaseLarvae(level);
    }

    // Larvae scatter out of the body in random directions
    private void releaseLarvae(ServerLevel level) {
        int count = LARVAE_MIN + random.nextInt(LARVAE_MAX - LARVAE_MIN + 1);
        for (int i = 0; i < count; i++) {
            BroodLarvaEntity larva = ModEntities.BROOD_LARVA.create(level, EntitySpawnReason.MOB_SUMMONED);
            if (larva == null) continue;
            float yaw = random.nextFloat() * 360.0F;
            larva.snapTo(getX(), getY() + 0.2, getZ(), yaw, 0.0F);
            larva.finalizeSpawn(level, level.getCurrentDifficultyAt(blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
            larva.setDeltaMovement(-Mth.sin(yaw * Mth.DEG_TO_RAD) * 0.2, 0.25, Mth.cos(yaw * Mth.DEG_TO_RAD) * 0.2);
            level.addFreshEntity(larva);
        }
        playSound(SoundEvents.SLIME_SQUISH, 1.0F, 1.4F);
    }

    // No skeleton jockeys: vanilla's spawn code mounts the skeleton with startRiding, which this refuses
    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        LivingEntity target = getTarget();
        if (spitWindup > 0) {
            tickSpitWindup(level, target);
            return;
        }
        if (spitCooldown > 0) {
            spitCooldown--;
            return;
        }
        if (canSpitAt(level, target)) {
            spitWindup = SPIT_WINDUP_TICKS;
            playSound(SoundEvents.SPIDER_AMBIENT, 1.5F, 0.5F);
        }
    }

    private boolean canSpitAt(ServerLevel level, LivingEntity target) {
        if (target == null || !target.isAlive()) return false;
        double distance = distanceTo(target);
        if (distance < SPIT_MIN_RANGE || distance > SPIT_MAX_RANGE || !getSensing().hasLineOfSight(target)) return false;
        // Already stuck: save the spit
        return !(level.getBlockState(target.blockPosition()).getBlock() instanceof WebBlock);
    }

    // Plants itself facing the target with web dripping from its mouth, then spits (or gives up if the target got away)
    private void tickSpitWindup(ServerLevel level, LivingEntity target) {
        spitWindup--;
        getNavigation().stop();
        getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0.0);
        if (target != null) getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (spitWindup % 3 == 0) {
            Vec3 mouth = getEyePosition().add(calculateViewVector(0.0F, getYHeadRot()).scale(getBbWidth() * 0.5));
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.BROOD_COBWEB.defaultBlockState()),
                mouth.x, mouth.y, mouth.z, 2, 0.08, 0.05, 0.08, 0.0);
        }
        if (spitWindup > 0) return;
        if (canSpitAt(level, target)) spitWeb(level, target);
        spitCooldown = SPIT_COOLDOWN_MIN + random.nextInt(SPIT_COOLDOWN_SPREAD);
    }

    private void spitWeb(ServerLevel level, LivingEntity target) {
        WebSpitEntity spit = new WebSpitEntity(level, this);
        double dx = target.getX() - getX();
        double dy = target.getY(SPIT_AIM_HEIGHT) - spit.getY();
        double dz = target.getZ() - getZ();
        // Lift the aim by roughly how far the glob will drop on the way (gravity 0.03 per tick, over distance / speed ticks)
        double flightTicks = Math.sqrt(dx * dx + dz * dz) / SPIT_SPEED;
        spit.shoot(dx, dy + 0.5 * 0.03 * flightTicks * flightTicks, dz, SPIT_SPEED, SPIT_INACCURACY);
        level.addFreshEntity(spit);
        playSound(SoundEvents.LLAMA_SPIT, 1.0F, 0.6F + random.nextFloat() * 0.2F);
    }

    @Override
    protected float getSoundVolume() {
        return 1.2F;
    }

    @Override
    public float getVoicePitch() {
        return 0.75F;
    }
}
