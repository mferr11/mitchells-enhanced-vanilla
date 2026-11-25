package mitchellenhancedvanilla.modid;

import mitchellenhancedvanilla.modid.entity.AmethystArrowEntity;
import mitchellenhancedvanilla.modid.entity.BarrowKingEntity;
import mitchellenhancedvanilla.modid.entity.BarrowServantEntity;
import mitchellenhancedvanilla.modid.entity.CopperArrowEntity;
import mitchellenhancedvanilla.modid.entity.EmeraldArrowEntity;
import mitchellenhancedvanilla.modid.entity.FrozenArrowEntity;
import mitchellenhancedvanilla.modid.entity.GoldArrowEntity;
import mitchellenhancedvanilla.modid.entity.PrismarineArrowEntity;
import mitchellenhancedvanilla.modid.entity.ReinforcedArrowEntity;
import mitchellenhancedvanilla.modid.entity.SoulFlameEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;

public class ModEntities {
    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, MitchellEnhancedVanilla.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    // Same settings as vanilla arrows
    public static final EntityType<ReinforcedArrowEntity> REINFORCED_ARROW = register("reinforced_arrow",
        EntityType.Builder.<ReinforcedArrowEntity>of(ReinforcedArrowEntity::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.5F, 0.5F)
            .eyeHeight(0.13F)
            .clientTrackingRange(4)
            .updateInterval(20));

    public static final EntityType<FrozenArrowEntity> FROZEN_ARROW = register("frozen_arrow",
        EntityType.Builder.<FrozenArrowEntity>of(FrozenArrowEntity::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.5F, 0.5F)
            .eyeHeight(0.13F)
            .clientTrackingRange(4)
            .updateInterval(20));

    public static final EntityType<CopperArrowEntity> COPPER_ARROW = register("copper_arrow",
        EntityType.Builder.<CopperArrowEntity>of(CopperArrowEntity::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.5F, 0.5F)
            .eyeHeight(0.13F)
            .clientTrackingRange(4)
            .updateInterval(20));

    public static final EntityType<AmethystArrowEntity> AMETHYST_ARROW = register("amethyst_arrow",
        EntityType.Builder.<AmethystArrowEntity>of(AmethystArrowEntity::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.5F, 0.5F)
            .eyeHeight(0.13F)
            .clientTrackingRange(4)
            .updateInterval(20));

    public static final EntityType<GoldArrowEntity> GOLD_ARROW = register("gold_arrow",
        EntityType.Builder.<GoldArrowEntity>of(GoldArrowEntity::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.5F, 0.5F)
            .eyeHeight(0.13F)
            .clientTrackingRange(4)
            .updateInterval(20));

    public static final EntityType<PrismarineArrowEntity> PRISMARINE_ARROW = register("prismarine_arrow",
        EntityType.Builder.<PrismarineArrowEntity>of(PrismarineArrowEntity::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.5F, 0.5F)
            .eyeHeight(0.13F)
            .clientTrackingRange(4)
            .updateInterval(20));

    public static final EntityType<EmeraldArrowEntity> EMERALD_ARROW = register("emerald_arrow",
        EntityType.Builder.<EmeraldArrowEntity>of(EmeraldArrowEntity::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.5F, 0.5F)
            .eyeHeight(0.13F)
            .clientTrackingRange(4)
            .updateInterval(20));

    // Wither skeleton's size and model, 150 HP boss
    public static final EntityType<BarrowKingEntity> BARROW_KING = register("barrow_king",
        EntityType.Builder.<BarrowKingEntity>of(BarrowKingEntity::new, MobCategory.MONSTER)
            .sized(0.7F, 2.4F)
            .eyeHeight(2.1F)
            .clientTrackingRange(10)
            .notInPeaceful());

    // Same size as a vanilla skeleton
    public static final EntityType<BarrowServantEntity> BARROW_SERVANT = register("barrow_servant",
        EntityType.Builder.<BarrowServantEntity>of(BarrowServantEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.99F)
            .eyeHeight(1.74F)
            .ridingOffset(-0.7F)
            .clientTrackingRange(8)
            .notInPeaceful());

    // Small-fireball-sized hitbox; drawn entirely with particles
    public static final EntityType<SoulFlameEntity> SOUL_FLAME = register("soul_flame",
        EntityType.Builder.<SoulFlameEntity>of(SoulFlameEntity::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.3125F, 0.3125F)
            .clientTrackingRange(4)
            .updateInterval(10));

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(BARROW_KING, BarrowKingEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(BARROW_SERVANT, BarrowServantEntity.createAttributes());
        // Natural spawns (barrow graveyards at night) follow vanilla skeleton rules: dark, on solid ground
        SpawnPlacements.register(BARROW_SERVANT, SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
    }
}
