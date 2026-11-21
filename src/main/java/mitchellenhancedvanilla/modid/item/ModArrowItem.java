package mitchellenhancedvanilla.modid.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

// Shared item for the mod's arrows; pass in the entity's constructors
public class ModArrowItem extends ArrowItem {
    // Matches the entity's (level, owner, pickupItem, weapon) constructor
    @FunctionalInterface
    public interface ShotFactory {
        AbstractArrow create(Level level, LivingEntity owner, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon);
    }

    // Matches the entity's (level, x, y, z, pickupItem, weapon) constructor
    @FunctionalInterface
    public interface DispenseFactory {
        AbstractArrow create(Level level, double x, double y, double z, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon);
    }

    private final ShotFactory shotFactory;
    private final DispenseFactory dispenseFactory;

    public ModArrowItem(Properties properties, ShotFactory shotFactory, DispenseFactory dispenseFactory) {
        super(properties);
        this.shotFactory = shotFactory;
        this.dispenseFactory = dispenseFactory;
    }

    // Fired from a bow or crossbow
    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity owner, @Nullable ItemStack firedFromWeapon) {
        return shotFactory.create(level, owner, ammo.copyWithCount(1), firedFromWeapon);
    }

    // Fired from a dispenser
    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        AbstractArrow arrow = dispenseFactory.create(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1), null);
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        return arrow;
    }
}
