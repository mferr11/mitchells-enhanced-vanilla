package mitchellenhancedvanilla.modid.block;

import mitchellenhancedvanilla.modid.entity.BroodLarvaEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;

// Brood Spider's web: slows like a cobweb, but falls apart on its own shortly after it lands
public class BroodCobwebBlock extends WebBlock {
    // How long it holds before dissolving (ticks)
    public static final int LIFETIME = 60;

    public BroodCobwebBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // Scheduled ticks are saved with the chunk, so it still dissolves after a reload
        if (!level.isClientSide()) level.scheduleTick(pos, this, LIFETIME);
    }

    // Spiders walk through it, as they do vanilla cobwebs, and so do brood larvae
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (!(entity instanceof Spider || entity instanceof BroodLarvaEntity)) super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Break particles and sound, no drops
        level.destroyBlock(pos, false);
    }
}
