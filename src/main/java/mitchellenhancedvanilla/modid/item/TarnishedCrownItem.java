package mitchellenhancedvanilla.modid.item;

import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.ModTags;
import mitchellenhancedvanilla.modid.entity.BarrowKingEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

// Summons the Barrow King: at night, under open sky, in a plains-type biome
public class TarnishedCrownItem extends Item {
    // He claws out of the ground this far in front of the player (blocks)
    public static final double SPAWN_DISTANCE = 6.0;
    // Spots further above or below the player than this are skipped (blocks)
    public static final int MAX_SPAWN_HEIGHT_DIFFERENCE = 4;
    // Only one King awake within this range (blocks)
    public static final double ONE_KING_RADIUS = 128.0;

    private static final String LANG = "item.mitchells-enhanced-vanilla.tarnished_crown.";

    public TarnishedCrownItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        Component failure = checkConditions(level, player);
        if (failure != null) {
            player.sendOverlayMessage(failure.copy().withStyle(ChatFormatting.GRAY));
            return InteractionResult.FAIL;
        }
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;

        BlockPos ground = findGround(server, player);
        BarrowKingEntity king = ModEntities.BARROW_KING.create(server, EntitySpawnReason.TRIGGERED);
        if (king == null) return InteractionResult.FAIL;

        king.finalizeSpawn(server, server.getCurrentDifficultyAt(ground), EntitySpawnReason.TRIGGERED, null);
        king.beginRising(ground);
        // Rises facing the summoner
        float yaw = (float) Math.toDegrees(Math.atan2(player.getZ() - king.getZ(), player.getX() - king.getX())) - 90.0F;
        king.snapTo(king.getX(), king.getY(), king.getZ(), yaw, 0.0F);
        king.setYHeadRot(yaw);
        king.setYBodyRot(yaw);
        server.addFreshEntity(king);

        server.playSound(null, ground, SoundEvents.WARDEN_EMERGE, SoundSource.HOSTILE, 2.0F, 0.7F);
        Component awoken = Component.translatable("entity.mitchells-enhanced-vanilla.barrow_king.awoken")
            .withStyle(BarrowKingEntity.MESSAGE_COLOUR);
        BarrowKingEntity.broadcast(server, Vec3.atCenterOf(ground), awoken);

        player.getItemInHand(hand).consume(1, player);
        return InteractionResult.SUCCESS_SERVER;
    }

    // Null when the King can be summoned, otherwise why not
    private static @Nullable Component checkConditions(Level level, Player player) {
        BlockPos pos = player.blockPosition();
        if (level.getDifficulty() == Difficulty.PEACEFUL) return Component.translatable(LANG + "peaceful");
        if (!level.isDarkOutside()) return Component.translatable(LANG + "not_night");
        if (!level.canSeeSky(pos.above())) return Component.translatable(LANG + "no_sky");
        if (!level.getBiome(pos).is(ModTags.BARROW_KING_BIOMES)) return Component.translatable(LANG + "wrong_biome");
        if (!level.getEntitiesOfClass(BarrowKingEntity.class, player.getBoundingBox().inflate(ONE_KING_RADIUS)).isEmpty()) {
            return Component.translatable(LANG + "already_awake");
        }
        return null;
    }

    // Surface a few steps in front of the player, stepping closer if that spot is a roof, tree or ravine
    private static BlockPos findGround(ServerLevel level, Player player) {
        Vec3 forward = Vec3.directionFromRotation(0.0F, player.getYRot());
        for (double distance = SPAWN_DISTANCE; distance > 0.0; distance -= 2.0) {
            int x = Mth.floor(player.getX() + forward.x * distance);
            int z = Mth.floor(player.getZ() + forward.z * distance);
            BlockPos ground = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            if (Math.abs(ground.getY() - player.getBlockY()) <= MAX_SPAWN_HEIGHT_DIFFERENCE) return ground;
        }
        // Nothing level ahead: rise right at the player's feet
        return player.blockPosition();
    }
}
