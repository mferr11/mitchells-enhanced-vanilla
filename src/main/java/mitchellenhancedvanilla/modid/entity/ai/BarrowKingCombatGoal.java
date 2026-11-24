package mitchellenhancedvanilla.modid.entity.ai;

import java.util.EnumSet;

import mitchellenhancedvanilla.modid.entity.BarrowKingEntity;
import mitchellenhancedvanilla.modid.entity.BarrowKingEntity.Pose;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

// Phase 1 duel: sword combos with clear wind-ups, and a telegraphed charge
public class BarrowKingCombatGoal extends Goal {
    // Sword combo
    public static final int COMBO_MIN = 2;
    public static final int COMBO_MAX = 3;
    public static final int FIRST_WINDUP_TICKS = 14;
    public static final int FOLLOWUP_WINDUP_TICKS = 12;
    // Arm stays forward while the swing animation plays (vanilla swing is 6 ticks)
    public static final int FOLLOW_THROUGH_TICKS = 6;
    // Small step forward on each swing
    public static final double LUNGE_SPEED = 0.5;
    // Shield down after a combo: the melee punish window
    public static final int COMBO_RECOVER_TICKS = 20;
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
    // Shield knocked aside by an axe (vanilla disables player shields for 5s)
    public static final int GUARD_BREAK_STUN_TICKS = 60;

    public static final double CHASE_SPEED = 1.0;
    // Keeps pressing forward through a combo, a little slower than his chase
    public static final double COMBO_MOVE_SPEED = 0.8;
    public static final int REPATH_INTERVAL = 10;

    private enum State { CHASE, WINDUP, RECOVER, CHARGE_WINDUP, CHARGING, STUNNED }

    private final BarrowKingEntity king;
    private State state = State.CHASE;
    private int timer;
    private int comboLeft;
    private int attackCooldown;
    private int chargeCooldown = CHARGE_COOLDOWN_TICKS / 2;
    private Vec3 chargeDir = Vec3.ZERO;

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
        enter(State.STUNNED, GUARD_BREAK_STUN_TICKS);
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
        LivingEntity target = king.getTarget();
        ServerLevel level = getServerLevel(king);
        timer--;

        switch (state) {
            case CHASE -> tickChase(target);
            case WINDUP -> tickWindup(level, target);
            case RECOVER, STUNNED -> {
                if (state == State.STUNNED && timer % 10 == 0) {
                    level.sendParticles(ParticleTypes.CRIT, king.getX(), king.getY(1.05), king.getZ(), 4, 0.3, 0.1, 0.3, 0.0);
                }
                if (timer <= 0) enter(State.CHASE, 0);
            }
            case CHARGE_WINDUP -> tickChargeWindup(level, target);
            case CHARGING -> tickCharging(level, target);
        }
    }

    private void tickChase(LivingEntity target) {
        if (target == null) return;
        king.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double distance = king.distanceTo(target);

        if (king.isWithinMeleeAttackRange(target) && attackCooldown <= 0) {
            comboLeft = COMBO_MIN + king.getRandom().nextInt(COMBO_MAX - COMBO_MIN + 1);
            king.playSound(SoundEvents.WITHER_SKELETON_AMBIENT, 1.5F, 0.5F);
            enter(State.WINDUP, FIRST_WINDUP_TICKS);
            return;
        }
        if (chargeCooldown <= 0 && distance >= CHARGE_MIN_RANGE && distance <= CHARGE_MAX_RANGE
            && king.getSensing().hasLineOfSight(target)) {
            king.playSound(SoundEvents.RAVAGER_ROAR, 2.0F, 0.6F);
            enter(State.CHARGE_WINDUP, CHARGE_WINDUP_TICKS);
            return;
        }
        if (king.getNavigation().isDone() || timer % REPATH_INTERVAL == 0) {
            king.getNavigation().moveTo(target, CHASE_SPEED);
        }
    }

    // Sword raised overhead, then the swing lands
    private void tickWindup(ServerLevel level, LivingEntity target) {
        if (target != null) {
            king.getLookControl().setLookAt(target, 30.0F, 30.0F);
            // Walk after a backpedalling target, but don't shove into one already in reach
            if (king.isWithinMeleeAttackRange(target)) {
                king.getNavigation().stop();
            } else if (king.getNavigation().isDone() || timer % REPATH_INTERVAL == 0) {
                king.getNavigation().moveTo(target, COMBO_MOVE_SPEED);
            }
        }
        // Follow-through done: raise the sword for the next swing
        if (king.getKingPose() == Pose.STRIKE && timer <= FOLLOWUP_WINDUP_TICKS - FOLLOW_THROUGH_TICKS) {
            king.setKingPose(Pose.WINDUP);
        }
        if (timer > 0) return;

        Vec3 forward = king.getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
        king.setDeltaMovement(king.getDeltaMovement().add(forward.scale(LUNGE_SPEED)));
        king.swingForAttack(InteractionHand.MAIN_HAND);
        king.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.6F);
        if (target != null && target.isAlive() && king.isWithinMeleeAttackRange(target)) {
            king.doHurtTarget(level, target);
        }

        if (--comboLeft > 0) {
            enter(State.WINDUP, FOLLOWUP_WINDUP_TICKS);
            king.setKingPose(Pose.STRIKE);
        } else {
            attackCooldown = ATTACK_COOLDOWN_TICKS;
            enter(State.RECOVER, COMBO_RECOVER_TICKS);
        }
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
        chargeCooldown = CHARGE_COOLDOWN_TICKS;
        enter(State.CHARGING, CHARGE_MAX_TICKS);
    }

    private void tickCharging(ServerLevel level, LivingEntity target) {
        double speedScale = king.getAttributeValue(Attributes.MOVEMENT_SPEED) / BarrowKingEntity.MOVEMENT_SPEED;
        Vec3 velocity = chargeDir.scale(CHARGE_SPEED * speedScale);
        king.setDeltaMovement(velocity.x, king.getDeltaMovement().y, velocity.z);
        faceTowards(king.position().add(chargeDir));
        dustAtFeet(level, 3);

        if (target != null && target.isAlive() && king.getBoundingBox().inflate(0.4).intersects(target.getBoundingBox())) {
            if (target.hurtServer(level, king.damageSources().mobAttack(king), CHARGE_DAMAGE)) {
                target.knockback(CHARGE_KNOCKBACK, -chargeDir.x, -chargeDir.z, king.damageSources().mobAttack(king), CHARGE_DAMAGE);
                target.needsSync = true;
            }
            king.playSound(SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, 1.0F, 0.7F);
            endCharge(State.RECOVER, CHARGE_HIT_RECOVER_TICKS);
        } else if (king.horizontalCollision) {
            // Slammed into a wall: dazed
            king.playSound(SoundEvents.ANVIL_LAND, 1.0F, 0.6F);
            dustAtFeet(level, 30);
            endCharge(State.STUNNED, CHARGE_WALL_STUN_TICKS);
        } else if (timer <= 0) {
            endCharge(State.STUNNED, CHARGE_MISS_STUN_TICKS);
        }
    }

    private void endCharge(State next, int ticks) {
        king.setDeltaMovement(0.0, king.getDeltaMovement().y, 0.0);
        enter(next, ticks);
    }

    private void enter(State next, int ticks) {
        state = next;
        timer = ticks;
        king.setKingPose(switch (next) {
            case CHASE -> Pose.GUARD;
            case WINDUP -> Pose.WINDUP;
            case CHARGE_WINDUP, CHARGING -> Pose.BRACE;
            case RECOVER, STUNNED -> Pose.STUNNED;
        });
        // Only the chase and sword combo move under their own power
        if (next != State.CHASE && next != State.WINDUP) king.getNavigation().stop();
    }

    private void faceTowards(Vec3 point) {
        float yaw = (float) (Mth.atan2(point.z - king.getZ(), point.x - king.getX()) * Mth.RAD_TO_DEG) - 90.0F;
        king.setYRot(yaw);
        king.setYBodyRot(yaw);
        king.setYHeadRot(yaw);
    }

    private void dustAtFeet(ServerLevel level, int count) {
        BlockState ground = level.getBlockState(king.blockPosition().below());
        if (ground.isAir()) return;
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
            king.getX(), king.getY() + 0.1, king.getZ(), count, 0.4, 0.05, 0.4, 0.1);
    }
}
