package mitchellenhancedvanilla.modid;

import mitchellenhancedvanilla.modid.entity.AmethystArrowEntity;
import mitchellenhancedvanilla.modid.entity.BarrowKingEntity;
import mitchellenhancedvanilla.modid.entity.CopperArrowEntity;
import mitchellenhancedvanilla.modid.entity.EmeraldArrowEntity;
import mitchellenhancedvanilla.modid.entity.FrozenArrowEntity;
import mitchellenhancedvanilla.modid.entity.GoldArrowEntity;
import mitchellenhancedvanilla.modid.entity.PrismarineArrowEntity;
import mitchellenhancedvanilla.modid.entity.ReinforcedArrowEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

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

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(BARROW_KING, BarrowKingEntity.createAttributes());
    }
}
