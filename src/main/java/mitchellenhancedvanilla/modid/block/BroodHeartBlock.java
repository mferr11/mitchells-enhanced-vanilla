package mitchellenhancedvanilla.modid.block;

import mitchellenhancedvanilla.modid.BroodSpawning;
import mitchellenhancedvanilla.modid.ModBlocks;
import mitchellenhancedvanilla.modid.block.entity.BroodHeartBlockEntity;
import mitchellenhancedvanilla.modid.entity.BroodLarvaEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

// Brood nest centrepiece: sealed until the King falls, then bursts into larvae and drops Brood Heart Fragments when broken
public class BroodHeartBlock extends BaseEntityBlock {
    public static final BooleanProperty SEALED = BooleanProperty.create("sealed");
    public static final int BREAK_LARVAE = 4;
    public static final int BREAK_XP = 20;

    public BroodHeartBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SEALED, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SEALED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BroodHeartBlockEntity(pos, state);
    }

    // Client only: plays the heartbeat in time with the texture
    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? createTickerHelper(type, ModBlocks.BROOD_HEART_ENTITY, BroodHeartBlockEntity::clientTick) : null;
    }

    // Sealed hearts can't be mined (creative can still break them)
    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return state.getValue(SEALED) ? 0.0F : super.getDestroyProgress(state, player, level, pos);
    }

    // Hitting it: bounces off while the King lives, opens it once he's dead
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel serverLevel) || player.isCreative()) return;
        Vec3 centre = Vec3.atCenterOf(pos);
        if (state.getValue(SEALED)) {
            if (!BroodSpawning.isKingDefeated(serverLevel)) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, centre.x, centre.y, centre.z, 6, 0.4, 0.4, 0.4, 0.01);
                level.playSound(null, pos, SoundEvents.SHIELD_BLOCK.value(), SoundSource.BLOCKS, 0.6F, 0.6F);
                return;
            }
            level.setBlock(pos, state.setValue(SEALED, false), Block.UPDATE_ALL);
        }
    }

    // Broken open: a last burst of larvae, plus xp
    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, tool, dropExperience);
        Vec3 centre = Vec3.atCenterOf(pos);
        if (level.getDifficulty() != Difficulty.PEACEFUL) BroodLarvaEntity.scatter(level, centre, BREAK_LARVAE);
        level.sendParticles(ParticleTypes.POOF, centre.x, centre.y, centre.z, 20, 0.4, 0.4, 0.4, 0.05);
        level.playSound(null, pos, SoundEvents.FROGSPAWN_HATCH, SoundSource.BLOCKS, 2.0F, 0.6F);
        if (dropExperience) popExperience(level, pos, BREAK_XP);
    }
}
