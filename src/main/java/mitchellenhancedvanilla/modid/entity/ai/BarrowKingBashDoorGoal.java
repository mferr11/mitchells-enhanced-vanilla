package mitchellenhancedvanilla.modid.entity.ai;

import mitchellenhancedvanilla.modid.entity.BarrowKingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

// Wooden doors don't stop him: he smashes through the door and the lintel above, since he's too tall for a doorway
public class BarrowKingBashDoorGoal extends Goal {
    // How far past his hitbox he looks for a door he's pressed against (blocks)
    private static final double REACH = 0.5;

    private final BarrowKingEntity king;
    private @Nullable BlockPos doorPos;

    public BarrowKingBashDoorGoal(BarrowKingEntity king) {
        this.king = king;
    }

    @Override
    public boolean canUse() {
        if (!king.canFight() || !king.horizontalCollision) return false;
        doorPos = findDoor();
        return doorPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        BlockPos pos = doorPos;
        if (pos == null || !(king.level() instanceof ServerLevel level)) return;
        BlockState door = level.getBlockState(pos);
        king.swingForAttack(king.getOffhandItem().isEmpty() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        level.playSound(null, pos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 1.5F, 0.8F);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, door),
            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 30, 0.3, 0.8, 0.3, 0.1);
        // With mob griefing off he only shoves it open
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            ((DoorBlock) door.getBlock()).setOpen(king, level, door, pos, true);
            return;
        }
        level.destroyBlock(pos, false, king);
        smashLintel(level, pos.above(2));
    }

    // The block over the doorway, unless it's open already or something even a wither can't break
    private void smashLintel(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getCollisionShape(level, pos).isEmpty() || state.is(BlockTags.WITHER_IMMUNE)) return;
        level.destroyBlock(pos, false, king);
    }

    // Lower half of a wooden door touching the front of his hitbox, if any
    private @Nullable BlockPos findDoor() {
        AABB box = king.getBoundingBox().inflate(REACH, 0.0, REACH);
        int y = king.getBlockY();
        for (BlockPos pos : BlockPos.betweenClosed(
                (int) Math.floor(box.minX), y, (int) Math.floor(box.minZ),
                (int) Math.floor(box.maxX), y + 1, (int) Math.floor(box.maxZ))) {
            if (!DoorBlock.isWoodenDoor(king.level(), pos)) continue;
            BlockState state = king.level().getBlockState(pos);
            return state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos.immutable();
        }
        return null;
    }
}
