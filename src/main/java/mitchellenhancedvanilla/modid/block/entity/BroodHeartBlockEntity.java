package mitchellenhancedvanilla.modid.block.entity;

import mitchellenhancedvanilla.modid.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BroodHeartBlockEntity extends BlockEntity {
    // Bumped by the client each time the heart texture's animation loops (see SpriteAnimationStateMixin)
    public static volatile int clientBeats;

    private int lastBeat = -1;

    public BroodHeartBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BROOD_HEART_ENTITY, pos, state);
    }

    // Thumps once per texture loop, on the frame the swell starts
    public static void clientTick(Level level, BlockPos pos, BlockState state, BroodHeartBlockEntity heart) {
        int beats = clientBeats;
        if (beats == heart.lastBeat) return;
        boolean first = heart.lastBeat < 0;
        heart.lastBeat = beats;
        if (!first) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.WARDEN_HEARTBEAT,
                SoundSource.BLOCKS, 1.0F, 0.8F, false);
        }
    }
}
