package mitchellenhancedvanilla.modid.mixin;

import mitchellenhancedvanilla.modid.CubeSteering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// Exposes the (protected) slime move control so decoys can steer it
@Mixin(targets = "net.minecraft.world.entity.monster.cubemob.AbstractCubeMob$CubeMobMoveControl")
public abstract class CubeMobMoveControlMixin implements CubeSteering {
    @Shadow public abstract void setDirection(float yRot, boolean isAggressive);
    @Shadow public abstract void setWantedMovement(double speedModifier);

    @Override
    public void mev$hopToward(float yRot, double speed) {
        setDirection(yRot, true);
        setWantedMovement(speed);
    }
}
