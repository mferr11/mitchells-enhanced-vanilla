package mitchellenhancedvanilla.modid.mixin;

import mitchellenhancedvanilla.modid.CubeSteering;
import mitchellenhancedvanilla.modid.DecoyBrain;
import mitchellenhancedvanilla.modid.DecoyTarget;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobDecoyMixin implements DecoyTarget {
    // Ticks between re-pathing toward the decoy
    @Unique private static final int REPATH_INTERVAL = 10;
    // Distracted mobs stop walking once this close (blocks)
    @Unique private static final int GAWK_DISTANCE = 2;
    @Unique private static final double GAWK_DISTANCE_SQR = (GAWK_DISTANCE + 0.5) * (GAWK_DISTANCE + 0.5);
    @Unique private static final float WALK_SPEED = 1.0F;

    @Shadow @Final protected GoalSelector targetSelector;
    @Shadow protected PathNavigation navigation;

    @Unique private @Nullable Entity mev$decoy;
    @Unique private int mev$decoyStart;
    @Unique private int mev$decoyUntil;

    @Override
    public void mev$distract(Entity decoy, int ticks) {
        Mob self = (Mob) (Object) this;
        if (mev$decoy != decoy) {
            mev$decoy = decoy;
            mev$decoyStart = self.tickCount;
            navigation.stop();
            if (mev$usesBrain()) {
                ((DecoyBrain) self.getBrain()).mev$setDecoyOwner(this);
                self.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            }
        }
        mev$decoyUntil = self.tickCount + ticks;
    }

    @Override
    public boolean mev$isDistracted() {
        return mev$activeDecoy() != null;
    }

    // Brain-driven mobs (piglins, hoglins, breezes...) have no target goals
    @Unique
    private boolean mev$usesBrain() {
        return targetSelector.getAvailableGoals().isEmpty();
    }

    @Unique
    private @Nullable Entity mev$activeDecoy() {
        Entity decoy = mev$decoy;
        if (decoy == null) return null;
        Mob self = (Mob) (Object) this;
        // Expired, gone, or the mob got hit since: snap out of it
        if (decoy.isRemoved() || self.tickCount > mev$decoyUntil
            || self.getLastHurtByMobTimestamp() > mev$decoyStart) {
            mev$decoy = null;
        }
        return mev$decoy;
    }

    // While distracted, goal-based mobs have no target, so attack goals stop
    @Inject(method = "getTarget", at = @At("HEAD"), cancellable = true)
    private void mev$hideTarget(CallbackInfoReturnable<LivingEntity> cir) {
        if (mev$activeDecoy() != null) cir.setReturnValue(null);
    }

    // Walk to and stare at the decoy, after goals and before the brain ticks
    @Inject(method = "serverAiStep", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;tick()V"))
    private void mev$approachDecoy(CallbackInfo ci) {
        Entity decoy = mev$activeDecoy();
        if (decoy == null) return;
        Mob self = (Mob) (Object) this;
        if (mev$usesBrain()) {
            Brain<?> brain = self.getBrain();
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(decoy, WALK_SPEED, GAWK_DISTANCE));
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(decoy, true));
            return;
        }
        boolean close = self.distanceToSqr(decoy) <= GAWK_DISTANCE_SQR;
        // Slimes and magma cubes hop in a set direction instead of pathfinding
        if (self.getMoveControl() instanceof CubeSteering cube) {
            float yRot = (float) (Mth.atan2(decoy.getZ() - self.getZ(), decoy.getX() - self.getX()) * Mth.RAD_TO_DEG) - 90.0F;
            cube.mev$hopToward(yRot, close ? 0.0 : WALK_SPEED);
            return;
        }
        if (close) {
            navigation.stop();
        } else if (navigation.isDone() || self.tickCount % REPATH_INTERVAL == 0) {
            navigation.moveTo(decoy, WALK_SPEED);
        }
        self.getLookControl().setLookAt(decoy);
    }
}
