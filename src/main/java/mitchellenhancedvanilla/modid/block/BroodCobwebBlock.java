package mitchellenhancedvanilla.modid.block;

import mitchellenhancedvanilla.modid.entity.BroodLarvaEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

// Brood web: slows like a cobweb. Spit webs fall apart on their own shortly after landing; crafted (permanent) ones stay
public class BroodCobwebBlock extends WebBlock {
    public enum Lifetime implements StringRepresentable {
        // A Brood Spider's spit, a player's Tome of Weaving cast, and crafted or worldgen webs
        SHORT("short", 60),
        LONG("long", 100),
        PERMANENT("permanent", 0);

        private final String name;
        // Ticks before it dissolves; 0 never
        final int ticks;

        Lifetime(String name, int ticks) {
            this.name = name;
            this.ticks = ticks;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final EnumProperty<Lifetime> LIFETIME = EnumProperty.create("lifetime", Lifetime.class);

    public BroodCobwebBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIFETIME, Lifetime.SHORT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIFETIME);
    }

    public BlockState withLifetime(Lifetime lifetime) {
        return defaultBlockState().setValue(LIFETIME, lifetime);
    }

    // Placed by hand (the crafted item): permanent
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withLifetime(Lifetime.PERMANENT);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // Scheduled ticks are saved with the chunk, so it still dissolves after a reload
        int ticks = state.getValue(LIFETIME).ticks;
        if (!level.isClientSide() && ticks > 0) level.scheduleTick(pos, this, ticks);
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
