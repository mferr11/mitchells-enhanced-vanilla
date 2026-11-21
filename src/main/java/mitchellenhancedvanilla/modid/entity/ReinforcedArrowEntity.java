package mitchellenhancedvanilla.modid.entity;

import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class ReinforcedArrowEntity extends ModArrowEntity {
    // Vanilla arrows have a base damage of 2.0
    public static final double BASE_DAMAGE = 4.0;

    public ReinforcedArrowEntity(EntityType<? extends ReinforcedArrowEntity> type, Level level) {
        super(type, level);
    }

    public ReinforcedArrowEntity(Level level, LivingEntity owner, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.REINFORCED_ARROW, level, owner, pickupItem, firedFromWeapon);
    }

    public ReinforcedArrowEntity(Level level, double x, double y, double z, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.REINFORCED_ARROW, level, x, y, z, pickupItem, firedFromWeapon);
    }

    @Override
    protected double baseDamage() {
        return BASE_DAMAGE;
    }

    @Override
    protected Item arrowItem() {
        return ModItems.REINFORCED_ARROW;
    }
}
