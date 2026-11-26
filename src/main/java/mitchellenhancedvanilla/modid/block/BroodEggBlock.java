package mitchellenhancedvanilla.modid.block;

import mitchellenhancedvanilla.modid.BroodSpawning;
import mitchellenhancedvanilla.modid.entity.BroodLarvaEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// Brood eggs, stacked 1 to 4 to a block like turtle eggs. Trampling pops one egg at a time; breaking the block pops them all.
// Each popped egg hatches larvae once the Barrow King has fallen (before that they just break). Silk Touch lifts them off intact
public class BroodEggBlock extends Block {
    public static final IntegerProperty EGGS = BlockStateProperties.EGGS;
    // Which way the clutch is turned; picked at random when first placed
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final int LARVAE_PER_EGG_MIN = 1;
    public static final int LARVAE_PER_EGG_MAX = 2;
    // Odds (1 in n) of an egg popping when landed on, and per step when walked across
    public static final int FALL_BURST_CHANCE = 1;
    public static final int STEP_BURST_CHANCE = 10;
    // Vanilla's turtle egg shapes scaled by 16/13, as the models are
    private static final VoxelShape SHAPE_SINGLE = Block.box(2.5, 0.0, 2.5, 13.5, 8.62, 13.5);
    private static final VoxelShape SHAPE_MULTIPLE = Block.box(0.0, 0.0, 0.0, 16.0, 8.62, 16.0);

    public BroodEggBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(EGGS, 1).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EGGS, FACING);
    }

    // Another egg onto an existing clutch, or a new clutch turned at random (seeded by position so the
    // placing player's prediction matches the server)
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        if (existing.is(this)) return existing.setValue(EGGS, Math.min(4, existing.getValue(EGGS) + 1));
        return withRandomFacing(RandomSource.create(Mth.getSeed(context.getClickedPos())));
    }

    // Clicking a clutch with more eggs adds to it, up to 4 (sneaking places against it instead)
    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (!context.isSecondaryUseActive() && context.getItemInHand().is(asItem()) && state.getValue(EGGS) < 4) return true;
        return super.canBeReplaced(state, context);
    }

    // A single egg turned at random; for worldgen, which sets EGGS itself
    public BlockState withRandomFacing(RandomSource random) {
        return defaultBlockState().setValue(FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(EGGS) == 1 ? SHAPE_SINGLE : SHAPE_MULTIPLE;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.isSteppingCarefully()) trample(level, pos, state, entity, STEP_BURST_CHANCE);
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        trample(level, pos, state, entity, FALL_BURST_CHANCE);
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    private void trample(Level level, BlockPos pos, BlockState state, Entity entity, int chance) {
        if (level instanceof ServerLevel server && canTrample(server, pos, entity) && server.getRandom().nextInt(chance) == 0) {
            popEgg(server, pos, state);
        }
    }

    // As turtle eggs: players outside spawn protection, and other mobs if mob griefing is on. The brood's own and bats never
    private static boolean canTrample(ServerLevel level, BlockPos pos, Entity entity) {
        if (entity instanceof Spider || entity instanceof BroodLarvaEntity || entity instanceof Bat) return false;
        if (entity instanceof Player player) return !level.getServer().isUnderSpawnProtection(level, pos, player);
        return entity instanceof LivingEntity && level.getGameRules().get(GameRules.MOB_GRIEFING);
    }

    // One egg cracks open (the last one takes the block with it), as vanilla turtle eggs
    private static void popEgg(ServerLevel level, BlockPos pos, BlockState state) {
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        int eggs = state.getValue(EGGS);
        if (eggs <= 1) {
            level.destroyBlock(pos, false);
        } else {
            level.setBlock(pos, state.setValue(EGGS, eggs - 1), Block.UPDATE_CLIENTS);
            level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(state));
            level.levelEvent(2001, pos, Block.getId(state));
        }
        hatch(level, pos, 1);
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, tool, dropExperience);
        if (level.getGameRules().get(GameRules.BLOCK_DROPS) && !EnchantmentHelper.hasTag(tool, EnchantmentTags.PREVENTS_INFESTED_SPAWNS)) {
            hatch(level, pos, state.getValue(EGGS));
        }
    }

    // Larvae scatter out of this many eggs, but only once the King has fallen
    private static void hatch(ServerLevel level, BlockPos pos, int eggs) {
        if (!BroodSpawning.isKingDefeated(level)) return;
        int count = 0;
        for (int i = 0; i < eggs; i++) count += LARVAE_PER_EGG_MIN + level.getRandom().nextInt(LARVAE_PER_EGG_MAX - LARVAE_PER_EGG_MIN + 1);
        BroodLarvaEntity.scatter(level, Vec3.atBottomCenterOf(pos), count);
        level.playSound(null, pos, SoundEvents.FROGSPAWN_HATCH, SoundSource.BLOCKS, 1.0F, 0.8F);
    }
}
