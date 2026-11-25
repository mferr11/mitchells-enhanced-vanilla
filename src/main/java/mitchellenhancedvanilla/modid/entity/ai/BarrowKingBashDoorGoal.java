package mitchellenhancedvanilla.modid.entity.ai;

import mitchellenhancedvanilla.modid.entity.BarrowKingEntity;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.DoorInteractGoal;

// Wooden doors don't stop him: he slams them open with his shield (or fist, once it's gone)
public class BarrowKingBashDoorGoal extends DoorInteractGoal {
    private final BarrowKingEntity king;

    public BarrowKingBashDoorGoal(BarrowKingEntity king) {
        super(king);
        this.king = king;
    }

    @Override
    public boolean canUse() {
        return king.canFight() && super.canUse() && !isOpen();
    }

    @Override
    public void start() {
        super.start();
        if (!(king.level() instanceof ServerLevel level)) return;
        king.swingForAttack(king.getOffhandItem().isEmpty() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        level.playSound(null, doorPos, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.HOSTILE, 1.5F, 0.8F);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(doorPos)),
            doorPos.getX() + 0.5, doorPos.getY() + 0.5, doorPos.getZ() + 0.5, 20, 0.3, 0.6, 0.3, 0.1);
        setOpen(true);
    }
}
