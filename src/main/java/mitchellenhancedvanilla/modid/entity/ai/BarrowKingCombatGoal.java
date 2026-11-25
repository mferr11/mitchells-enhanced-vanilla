package mitchellenhancedvanilla.modid.entity.ai;

import java.util.EnumSet;

import mitchellenhancedvanilla.modid.ModParticles;
import mitchellenhancedvanilla.modid.ScreenShakePayload;
import mitchellenhancedvanilla.modid.entity.BarrowKingEntity;
import mitchellenhancedvanilla.modid.entity.BarrowKingEntity.Pose;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

// The King's fight: sword combos and a telegraphed charge, servant waves in phase 2, faster and harder in phase 3
public class BarrowKingCombatGoal extends Goal {
    // Sword combo
    public static final int COMBO_MIN = 2;
    public static final int COMBO_MAX = 3;
    public static final int FIRST_WINDUP_TICKS = 14;
    public static final int FOLLOWUP_WINDUP_TICKS = 12;
    // Arm stays forward while the swing animation plays (vanilla swing is 6 ticks)
    public static final int FOLLOW_THROUGH_TICKS = 6;
    // Small step forward on each swing
    public static final double LUNGE_SPEED = 0.3;
    // Shield down after a combo: the melee punish window
    public static final int COMBO_RECOVER_TICKS = 30;
    public static final int ATTACK_COOLDOWN_TICKS = 60;

    // Charge: used when the target keeps its distance
    public static final double CHARGE_MIN_RANGE = 6.0;
    public static final double CHARGE_MAX_RANGE = 18.0;
    public static final int CHARGE_COOLDOWN_TICKS = 8 * 20;
    public static final int CHARGE_WINDUP_TICKS = 20;
    public static final int CHARGE_MAX_TICKS = 30;
    // Blocks per tick at full speed (slowed by Frozen Arrows like his walk is)
    public static final double CHARGE_SPEED = 0.7;
    public static final float CHARGE_DAMAGE = 10.0F;
    public static final double CHARGE_KNOCKBACK = 1.5;
    // Winded after a charge: the arrow window
    public static final int CHARGE_HIT_RECOVER_TICKS = 25;
    public static final int CHARGE_MISS_STUN_TICKS = 40;
    public static final int CHARGE_WALL_STUN_TICKS = 70;
    // Short, hard thump when he hits a wall: strength (degrees), length, reach (blocks)
    public static final float WALL_SHAKE_STRENGTH = 1.5F;
    public static final int WALL_SHAKE_TICKS = 8;
    public static final double WALL_SHAKE_RADIUS = 24.0;
    // Soul and smoke puffs left behind him each tick while charging
    public static final int CHARGE_TRAIL_POINTS = 3;
    // Wisp streak behind each sword lunge: length (blocks) and puffs
    public static final double LUNGE_TRAIL_LENGTH = 0.8;
    public static final int LUNGE_TRAIL_POINTS = 3;
    // Sweep-attack particle distance in front of him (players use 1 block; he is bigger and reaches further)
    public static final double SWEEP_DISTANCE = 1.4;
    // Shield knocked aside by an axe (vanilla disables player shields for 5s)
    public static final int GUARD_BREAK_STUN_TICKS = 40;

    // Phase 2: sword raised, shield down while the ground stirs, then a wave rises
    public static final int SUMMON_TICKS = 30;
    public static final int SUMMON_RECOVER_TICKS = 10;
    public static final int SERVANT_WAVE_INTERVAL = 15 * 20;

    // Phase 3: quicker wind-ups, longer combos and shorter cooldowns (stuns stay the same)
    public static final int ENRAGED_COMBO_MIN = 3;
    public static final int ENRAGED_COMBO_MAX = 4;
    public static final int ENRAGED_FIRST_WINDUP_TICKS = 9;
    public static final int ENRAGED_FOLLOWUP_WINDUP_TICKS = 10;
    public static final int ENRAGED_COMBO_RECOVER_TICKS = 12;
    public static final int ENRAGED_ATTACK_COOLDOWN_TICKS = 25;
    public static final int ENRAGED_CHARGE_COOLDOWN_TICKS = 5 * 20;
    // Roars in place as he throws the shield away
    public static final int ENRAGE_ROAR_TICKS = 25;

    public static final double CHASE_SPEED = 1.0;
    // Repath countdown, like vanilla melee mobs: normal, once he's reached the target, and after a failed search
    public static final int REPATH_INTERVAL = 10;
    public static final int REPATH_WHEN_ARRIVED = 4;
    public static final int REPATH_FAIL_DELAY = 15;

    // COMBO_RECOVER is the shield-down pause after a combo; RECOVER the planted one (charge hit, summon, enrage roar);
    // DAZED a real stun (wall, missed charge, guard break)
    private enum State {
        CHASE(Pose.GUARD, true),
        WINDUP(Pose.WINDUP, true),
        COMBO_RECOVER(Pose.RECOVER, true),
        RECOVER(Pose.RECOVER, false),
        CHARGE_WINDUP(Pose.BRACE, false),
        CHARGING(Pose.BRACE, false),
        DAZED(Pose.DAZED, false),
        SUMMON(Pose.SUMMON, false);

        final Pose pose;
        // Walks under his own power in this state
        final boolean moves;

        State(Pose pose, boolean moves) {
            this.pose = pose;
            this.moves = moves;
        }
    }

    private final BarrowKingEntity king;
    private State state = State.CHASE;
    private int timer;
    private int comboLeft;
    private int attackCooldown;
    private int chargeCooldown = CHARGE_COOLDOWN_TICKS / 2;
    private int summonCooldown = SERVANT_WAVE_INTERVAL;
    private Vec3 chargeDir = Vec3.ZERO;
    private int repathCooldown;
    private boolean lastPathFound = true;

    public BarrowKingCombatGoal(BarrowKingEntity king) {
        this.king = king;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = king.getTarget();
        // A pending stagger runs even without a target
        return king.canFight() && (state != State.CHASE || target != null && target.isAlive());
    }

    // An axe hit on the raised shield: staggered with his guard open, interrupting whatever he was doing
    public void breakGuard() {
        king.setDeltaMovement(0.0, king.getDeltaMovement().y, 0.0);
        enter(State.DAZED, GUARD_BREAK_STUN_TICKS);
    }

    // Phase 2 just started: call the first wave as soon as he is free
    public void raiseServantsSoon() {
        summonCooldown = 0;
    }

    // Phase 3 just started: interrupt everything for the roar, charge soon after
    public void onEnraged() {
        king.setDeltaMovement(0.0, king.getDeltaMovement().y, 0.0);
        enter(State.RECOVER, ENRAGE_ROAR_TICKS);
        chargeCooldown = Math.min(chargeCooldown, ENRAGE_ROAR_TICKS + 20);
    }

    private boolean enraged() {
        return king.getPhase() == BarrowKingEntity.Phase.ENRAGED;
    }

    private int followupWindupTicks() {
        return enraged() ? ENRAGED_FOLLOWUP_WINDUP_TICKS : FOLLOWUP_WINDUP_TICKS;
    }

    // Combos and charges always play out, even if the target is lost mid-swing
    @Override
    public boolean canContinueToUse() {
        return king.canFight() && (state != State.CHASE || canUse());
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        // Keep a guard break that landed while the goal was idle
        if (state == State.CHASE) enter(State.CHASE, 0);
        king.setAggressive(true);
    }

    @Override
    public void stop() {
        king.setKingPose(Pose.GUARD);
        king.setAggressive(false);
        king.getNavigation().stop();
        state = State.CHASE;
    }

    @Override
    public void tick() {
        if (attackCooldown > 0) attackCooldown--;
        if (chargeCooldown > 0) chargeCooldown--;
        if (summonCooldown > 0) summonCooldown--;
        LivingEntity target = king.getTarget();
        ServerLevel level = getServerLevel(king);
        timer--;

        switch (state) {
            case CHASE -> tickChase(level, target);
            case SUMMON -> tickSummon(level, target);
            case WINDUP -> tickWindup(level, target);
            case COMBO_RECOVER -> {
                // Shield still down, but he keeps walking after the target
                followTarget(target);
                if (timer <= 0) enter(State.CHASE, 0);
            }
            case RECOVER, DAZED -> {
                if (state == State.DAZED && timer % 10 == 0) {
                    level.sendParticles(ParticleTypes.CRIT, king.getX(), king.getY(1.05), king.getZ(), 4, 0.3, 0.1, 0.3, 0.0);
                }
                if (timer <= 0) enter(State.CHASE, 0);
            }
            case CHARGE_WINDUP -> tickChargeWindup(level, target);
            case CHARGING -> tickCharging(level, target);
        }
    }

    private void tickChase(ServerLevel level, LivingEntity target) {
        if (target == null) return;
        king.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double distance = king.distanceTo(target);

        // Servant count is checked twice a second rather than every tick
        if (summonCooldown <= 0 && king.tickCount % 10 == 0 && king.canRaiseServants(level)) {
            king.playSound(SoundEvents.WITHER_SKELETON_AMBIENT, 2.0F, 0.4F);
            enter(State.SUMMON, SUMMON_TICKS);
            return;
        }

        if (king.isWithinMeleeAttackRange(target) && attackCooldown <= 0) {
            int min = enraged() ? ENRAGED_COMBO_MIN : COMBO_MIN;
            int max = enraged() ? ENRAGED_COMBO_MAX : COMBO_MAX;
            comboLeft = min + king.getRandom().nextInt(max - min + 1);
            king.playSound(SoundEvents.WITHER_SKELETON_AMBIENT, 1.5F, 0.5F);
            enter(State.WINDUP, enraged() ? ENRAGED_FIRST_WINDUP_TICKS : FIRST_WINDUP_TICKS);
            return;
        }
        if (chargeCooldown <= 0 && distance >= CHARGE_MIN_RANGE && distance <= CHARGE_MAX_RANGE
            && king.getSensing().hasLineOfSight(target)) {
            king.roar(level, 2.0F, 0.6F);
            enter(State.CHARGE_WINDUP, CHARGE_WINDUP_TICKS);
            return;
        }
        followTarget(target);
    }

    // Sword raised overhead, then the swing lands
    private void tickWindup(ServerLevel level, LivingEntity target) {
        // Keeps pressing in at full chase speed while swinging, like vanilla melee mobs
        followTarget(target);
        // Follow-through done: raise the sword for the next swing
        if (king.getKingPose() == Pose.STRIKE && timer <= followupWindupTicks() - FOLLOW_THROUGH_TICKS) {
            king.setKingPose(Pose.WINDUP);
        }
        if (timer > 0) return;

        Vec3 forward = king.getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
        king.setDeltaMovement(king.getDeltaMovement().add(forward.scale(LUNGE_SPEED)));
        // Short wisp streak behind the step
        BarrowKingEntity.soulStreak(level, king.position(), forward.scale(-1.0), LUNGE_TRAIL_LENGTH, LUNGE_TRAIL_POINTS, king.getY(0.55));
        swingSword(level);
        king.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.6F);
        if (target != null && target.isAlive() && king.isWithinMeleeAttackRange(target)) {
            king.doHurtTarget(level, target);
        }

        if (--comboLeft > 0) {
            enter(State.WINDUP, followupWindupTicks());
            king.setKingPose(Pose.STRIKE);
        } else {
            attackCooldown = enraged() ? ENRAGED_ATTACK_COOLDOWN_TICKS : ATTACK_COOLDOWN_TICKS;
            enter(State.COMBO_RECOVER, enraged() ? ENRAGED_COMBO_RECOVER_TICKS : COMBO_RECOVER_TICKS);
        }
    }

    // Sword held high while grave-dust swirls around him, then the wave rises
    private void tickSummon(ServerLevel level, LivingEntity target) {
        if (target != null) king.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (timer % 3 == 0) {
            double angle = king.getRandom().nextDouble() * Math.PI * 2.0;
            double dist = BarrowKingEntity.SERVANT_MIN_DISTANCE
                + king.getRandom().nextDouble() * (BarrowKingEntity.SERVANT_MAX_DISTANCE - BarrowKingEntity.SERVANT_MIN_DISTANCE);
            level.sendParticles(ParticleTypes.SOUL, king.getX() + Math.cos(angle) * dist, king.getY() + 0.2,
                king.getZ() + Math.sin(angle) * dist, 2, 0.2, 0.1, 0.2, 0.02);
        }
        if (timer > 0) return;

        king.raiseServants(level);
        summonCooldown = SERVANT_WAVE_INTERVAL;
        enter(State.RECOVER, SUMMON_RECOVER_TICKS);
    }

    // Swing animation plus a soul-blue version of the player's sweep-attack arc in front of him
    private void swingSword(ServerLevel level) {
        king.swingForAttack(InteractionHand.MAIN_HAND);
        float yaw = king.getYRot() * Mth.DEG_TO_RAD;
        double dx = -Mth.sin(yaw);
        double dz = Mth.cos(yaw);
        level.sendParticles(ModParticles.SOUL_SWEEP, king.getX() + dx * SWEEP_DISTANCE, king.getY(0.5), king.getZ() + dz * SWEEP_DISTANCE,
            0, dx, 0.0, dz, 0.0);
    }

    // Walks after the target at chase speed. Repaths on a countdown rather than whenever the path is done:
    // an unreachable target would otherwise cost a full path search every tick
    private void followTarget(LivingEntity target) {
        if (target == null) return;
        king.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (repathCooldown > 0) repathCooldown--;
        if (lastPathFound && king.getNavigation().isDone()) repathCooldown = Math.min(repathCooldown, REPATH_WHEN_ARRIVED);
        if (repathCooldown > 0) return;
        lastPathFound = king.getNavigation().moveTo(target, CHASE_SPEED);
        repathCooldown = lastPathFound ? REPATH_INTERVAL : REPATH_INTERVAL + REPATH_FAIL_DELAY;
    }

    // Plants his feet, roars and paws the ground, then commits to a straight line
    private void tickChargeWindup(ServerLevel level, LivingEntity target) {
        if (target != null) {
            king.getLookControl().setLookAt(target, 30.0F, 30.0F);
            faceTowards(target.position());
        }
        if (timer % 4 == 0) dustAtFeet(level, 6);
        if (timer > 0) return;

        Vec3 aim = target != null ? target.position() : king.position().add(king.getLookAngle());
        chargeDir = new Vec3(aim.x - king.getX(), 0.0, aim.z - king.getZ()).normalize();
        chargeCooldown = enraged() ? ENRAGED_CHARGE_COOLDOWN_TICKS : CHARGE_COOLDOWN_TICKS;
        enter(State.CHARGING, CHARGE_MAX_TICKS);
    }

    private void tickCharging(ServerLevel level, LivingEntity target) {
        double speedScale = king.getAttributeValue(Attributes.MOVEMENT_SPEED) / BarrowKingEntity.MOVEMENT_SPEED;
        Vec3 velocity = chargeDir.scale(CHARGE_SPEED * speedScale);
        king.setDeltaMovement(velocity.x, king.getDeltaMovement().y, velocity.z);
        faceTowards(king.position().add(chargeDir));
        dustAtFeet(level, 3);
        chargeTrail(level, velocity.length());

        if (target != null && target.isAlive() && king.getBoundingBox().inflate(0.4).intersects(target.getBoundingBox())) {
            if (target.hurtServer(level, king.damageSources().mobAttack(king), CHARGE_DAMAGE)) {
                target.knockback(CHARGE_KNOCKBACK, -chargeDir.x, -chargeDir.z, king.damageSources().mobAttack(king), CHARGE_DAMAGE);
                target.syncVelocity = true;
            }
            // The raised sword comes down as he connects
            swingSword(level);
            king.playSound(SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, 1.0F, 0.7F);
            endCharge(State.RECOVER, CHARGE_HIT_RECOVER_TICKS);
        } else if (king.horizontalCollision) {
            // Slammed into a wall: dazed
            king.playSound(SoundEvents.ANVIL_LAND, 1.0F, 0.6F);
            ScreenShakePayload.shakeNearby(level, king.position(), WALL_SHAKE_RADIUS, WALL_SHAKE_STRENGTH, WALL_SHAKE_TICKS);
            dustAtFeet(level, 30);
            endCharge(State.DAZED, CHARGE_WALL_STUN_TICKS);
        } else if (timer <= 0) {
            endCharge(State.DAZED, CHARGE_MISS_STUN_TICKS);
        }
    }

    // Ghostly streak behind him, spread over this tick's travel so the trail is continuous
    private void chargeTrail(ServerLevel level, double distanceThisTick) {
        Vec3 back = chargeDir.scale(-1.0);
        BarrowKingEntity.streak(level, ParticleTypes.SOUL, king.position(), back, distanceThisTick, CHARGE_TRAIL_POINTS, king.getY(0.55), 0.35);
        BarrowKingEntity.streak(level, ParticleTypes.SMOKE, king.position(), back, distanceThisTick, CHARGE_TRAIL_POINTS, king.getY(0.3), 0.25);
    }

    private void endCharge(State next, int ticks) {
        king.setDeltaMovement(0.0, king.getDeltaMovement().y, 0.0);
        enter(next, ticks);
    }

    private void enter(State next, int ticks) {
        state = next;
        timer = ticks;
        king.setKingPose(next.pose);
        if (!next.moves) king.getNavigation().stop();
    }

    private void faceTowards(Vec3 point) {
        float yaw = (float) (Mth.atan2(point.z - king.getZ(), point.x - king.getX()) * Mth.RAD_TO_DEG) - 90.0F;
        king.setYRot(yaw);
        king.setYBodyRot(yaw);
        king.setYHeadRot(yaw);
    }

    private void dustAtFeet(ServerLevel level, int count) {
        BarrowKingEntity.groundDust(level, king.blockPosition().below(), king.getX(), king.getZ(), count, 0.4, 0.1);
    }
}
