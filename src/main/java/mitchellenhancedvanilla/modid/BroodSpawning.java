package mitchellenhancedvanilla.modid;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.LightLayer;

// Brood Spiders only appear once the Barrow King has fallen: from then on they take over some natural spider spawns
public final class BroodSpawning {
    // Chance a natural spider spawn becomes a Brood Spider: anywhere else, and deep in caves
    public static final float SURFACE_CHANCE = 0.1F;
    public static final float UNDERGROUND_CHANCE = 0.5F;

    // Saved on the overworld, so it holds for the whole world and survives restarts
    private static final AttachmentType<Boolean> KING_DEFEATED =
        AttachmentRegistry.createPersistent(MitchellEnhancedVanilla.id("barrow_king_defeated"), Codec.BOOL);

    private BroodSpawning() {}

    // Loads the class so the attachment is registered before any world loads
    public static void initialize() {}

    public static void markKingDefeated(ServerLevel level) {
        level.getServer().overworld().setAttached(KING_DEFEATED, true);
    }

    public static boolean isKingDefeated(ServerLevel level) {
        return Boolean.TRUE.equals(level.getServer().overworld().getAttached(KING_DEFEATED));
    }

    // The type to spawn in place of a natural spawn of `type` at `pos`: a Brood Spider sometimes, otherwise unchanged
    public static EntityType<?> replaceNaturalSpawn(ServerLevel level, EntityType<?> type, BlockPos pos) {
        if (type != EntityTypes.SPIDER || !isKingDefeated(level)) return type;
        float chance = isUnderground(level, pos) ? UNDERGROUND_CHANCE : SURFACE_CHANCE;
        if (level.getRandom().nextFloat() >= chance) return type;
        // Vanilla only checked room for a spider; the bigger Brood Spider needs its own check
        EntityType<?> brood = ModEntities.BROOD_SPIDER;
        boolean fits = level.noCollision(brood.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
        return fits ? brood : type;
    }

    // Real caves: below sea level with no sky light reaching it (so not cave mouths, ravines open to the sky, or buildings)
    private static boolean isUnderground(ServerLevel level, BlockPos pos) {
        return pos.getY() < level.getSeaLevel() && level.getBrightness(LightLayer.SKY, pos) == 0;
    }
}
