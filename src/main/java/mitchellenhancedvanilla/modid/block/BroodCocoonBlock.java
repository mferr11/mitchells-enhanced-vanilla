package mitchellenhancedvanilla.modid.block;

import mitchellenhancedvanilla.modid.BroodSpawning;
import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.entity.BroodLarvaEntity;
import mitchellenhancedvanilla.modid.entity.BroodSpiderEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// A silk cocoon in a brood nest (2-3 per nest, out in the side pockets): bursts apart into Brood Spiders and a few larvae the
// first time a survival player comes near after the Barrow King has fallen. Dormant before that, and can't be mined, so it
// can't be broken early
public class BroodCocoonBlock extends Block {
    // How close a player has to come (blocks), and how often it checks (ticks)
    public static final double WAKE_RANGE = 12.0;
    public static final int CHECK_INTERVAL = 20;
    public static final int SPIDERS_MIN = 1;
    public static final int SPIDERS_MAX = 2;
    public static final int LARVAE_MIN = 1;
    public static final int LARVAE_MAX = 2;
    private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 14.0);

    public BroodCocoonBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // Unmineable (creative can still break it), like a spawner that can't be cheesed
    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return 0.0F;
    }

    // Placed by hand; worldgen schedules the first check itself (onPlace doesn't run there)
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) level.scheduleTick(pos, this, CHECK_INTERVAL);
    }

    // Checks every second for a survival or adventure player in range, until it bursts
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean awake = BroodSpawning.isKingDefeated(level)
            && level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, WAKE_RANGE, EntitySelector.NO_CREATIVE_OR_SPECTATOR) != null;
        if (awake) {
            burst(level, pos, state);
        } else {
            level.scheduleTick(pos, this, CHECK_INTERVAL);
        }
    }

    // The cocoon bursts and is gone: Brood Spiders climb out with a few larvae
    private static void burst(ServerLevel level, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
        Vec3 centre = Vec3.atBottomCenterOf(pos);
        int spiders = SPIDERS_MIN + level.getRandom().nextInt(SPIDERS_MAX - SPIDERS_MIN + 1);
        for (int i = 0; i < spiders; i++) {
            BroodSpiderEntity spider = ModEntities.BROOD_SPIDER.create(level, EntitySpawnReason.TRIGGERED);
            if (spider == null) continue;
            spider.snapTo(centre.x, centre.y, centre.z, level.getRandom().nextFloat() * 360.0F, 0.0F);
            spider.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.TRIGGERED, null);
            // Nest guardians: never despawn
            spider.setPersistenceRequired();
            level.addFreshEntity(spider);
        }
        BroodLarvaEntity.scatter(level, centre, LARVAE_MIN + level.getRandom().nextInt(LARVAE_MAX - LARVAE_MIN + 1));
        // Shell fragments flung out, plus a puff of brood silk
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), centre.x, centre.y + 0.5, centre.z, 60, 0.35, 0.4, 0.35, 0.15);
        level.sendParticles(ParticleTypes.POOF, centre.x, centre.y + 0.5, centre.z, 12, 0.3, 0.3, 0.3, 0.05);
        level.playSound(null, pos, SoundEvents.FROGSPAWN_HATCH, SoundSource.HOSTILE, 2.0F, 0.5F);
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.HOSTILE, 2.0F, 0.6F);
        level.playSound(null, pos, SoundEvents.SPIDER_AMBIENT, SoundSource.HOSTILE, 2.0F, 0.5F);
    }
}
