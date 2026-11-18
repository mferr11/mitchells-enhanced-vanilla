package mitchellenhancedvanilla.modid.entity;

import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class FrozenArrowEntity extends AbstractArrow {
    // Same as vanilla arrows for now. Hit effects can go in doPostHurtEffects later.

    public FrozenArrowEntity(EntityType<? extends FrozenArrowEntity> type, Level level) {
        super(type, level);
    }

    public FrozenArrowEntity(Level level, LivingEntity owner, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.FROZEN_ARROW, owner, level, pickupItem, firedFromWeapon);
    }

    public FrozenArrowEntity(Level level, double x, double y, double z, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.FROZEN_ARROW, x, y, z, level, pickupItem, firedFromWeapon);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.FROZEN_ARROW);
    }
}
