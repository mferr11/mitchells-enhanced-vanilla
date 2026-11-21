package mitchellenhancedvanilla.modid;

import mitchellenhancedvanilla.modid.entity.FrozenArrowEntity;
import mitchellenhancedvanilla.modid.entity.ReinforcedArrowEntity;
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

    public static void initialize() {
        // Loads the class so the fields above register
    }
}
