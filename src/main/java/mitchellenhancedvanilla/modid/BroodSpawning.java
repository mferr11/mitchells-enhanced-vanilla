package mitchellenhancedvanilla.modid;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LightLayer;
import org.jspecify.annotations.Nullable;
import mitchellenhancedvanilla.modid.worldgen.ModStructures;

// Brood Spiders only appear once the Barrow King has fallen: from then on they take over some natural spider spawns,
// and every monster spawn inside a brood nest
public final class BroodSpawning {
    // Chance a natural spider spawn becomes a Brood Spider: anywhere else, and deep in caves
    public static final float SURFACE_CHANCE = 0.1F;
    public static final float UNDERGROUND_CHANCE = 0.5F;

    // Saved on the overworld, so it holds for the whole world and survives restarts
    private static final AttachmentType<Boolean> KING_DEFEATED =
        AttachmentRegistry.createPersistent(MitchellEnhancedVanilla.id("barrow_king_defeated"), Codec.BOOL);

    // Pause after the King's defeat message before the unlock is announced
    public static final int ANNOUNCE_DELAY_TICKS = 3 * 20;

    // Ticks left until the announcement; 0 when none is pending
    private static int announceIn;

    private BroodSpawning() {}

    // Registers the attachment before any world loads, and the announcement countdown
    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (announceIn > 0 && --announceIn == 0) {
                server.getPlayerList().broadcastSystemMessage(
                    Component.translatable("message.mitchells-enhanced-vanilla.brood_unleashed")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> announceIn = 0);
    }

    // The first time only, tells every player on the server (shortly after) that something has changed
    public static void markKingDefeated(ServerLevel level) {
        if (isKingDefeated(level)) return;
        level.getServer().overworld().setAttached(KING_DEFEATED, true);
        announceIn = ANNOUNCE_DELAY_TICKS;
    }

    public static boolean isKingDefeated(ServerLevel level) {
        return Boolean.TRUE.equals(level.getServer().overworld().getAttached(KING_DEFEATED));
    }

    // The type to spawn in place of a natural spawn of `type` at `pos`: always a Brood Spider inside a nest (or nothing
    // where one won't fit), sometimes one in place of a spider elsewhere, otherwise unchanged
    public static @Nullable EntityType<?> replaceNaturalSpawn(ServerLevel level, EntityType<?> type, BlockPos pos) {
        if (type.getCategory() != MobCategory.MONSTER || !isKingDefeated(level)) return type;
        boolean nest = isInNest(level, pos);
        if (!nest) {
            if (type != EntityTypes.SPIDER) return type;
            float chance = isUnderground(level, pos) ? UNDERGROUND_CHANCE : SURFACE_CHANCE;
            if (level.getRandom().nextFloat() >= chance) return type;
        }
        // Vanilla only checked room for the original mob; the bigger Brood Spider needs its own check
        EntityType<?> brood = ModEntities.BROOD_SPIDER;
        boolean fits = level.noCollision(brood.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
        if (fits) return brood;
        return nest ? null : type;
    }

    public static boolean isInNest(ServerLevel level, BlockPos pos) {
        return level.structureManager().getStructureWithPieceAt(pos, structure -> structure.is(ModStructures.BROOD_NEST_KEY)).isValid();
    }

    // Real caves: below sea level with no sky light reaching it (so not cave mouths, ravines open to the sky, or buildings)
    private static boolean isUnderground(ServerLevel level, BlockPos pos) {
        return pos.getY() < level.getSeaLevel() && level.getBrightness(LightLayer.SKY, pos) == 0;
    }
}
