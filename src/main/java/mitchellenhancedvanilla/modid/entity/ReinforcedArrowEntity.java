package mitchellenhancedvanilla.modid.entity;

import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class ReinforcedArrowEntity extends AbstractArrow {
    // Vanilla arrows have a base damage of 2.0
    public static final double BASE_DAMAGE = 4.0;

    public ReinforcedArrowEntity(EntityType<? extends ReinforcedArrowEntity> type, Level level) {
        super(type, level);
        this.setBaseDamage(BASE_DAMAGE);
    }

    public ReinforcedArrowEntity(Level level, LivingEntity owner, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.REINFORCED_ARROW, owner, level, pickupItem, firedFromWeapon);
        this.setBaseDamage(BASE_DAMAGE);
    }

    public ReinforcedArrowEntity(Level level, double x, double y, double z, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.REINFORCED_ARROW, x, y, z, level, pickupItem, firedFromWeapon);
        this.setBaseDamage(BASE_DAMAGE);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.REINFORCED_ARROW);
    }
}
