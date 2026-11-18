package mitchellenhancedvanilla.modid.item;

import mitchellenhancedvanilla.modid.entity.ReinforcedArrowEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class ReinforcedArrowItem extends ArrowItem {
    public ReinforcedArrowItem(Properties properties) {
        super(properties);
    }

    // Fired from a bow or crossbow
    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity owner, @Nullable ItemStack firedFromWeapon) {
        return new ReinforcedArrowEntity(level, owner, ammo.copyWithCount(1), firedFromWeapon);
    }

    // Fired from a dispenser
    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        ReinforcedArrowEntity arrow = new ReinforcedArrowEntity(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1), null);
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        return arrow;
    }
}
