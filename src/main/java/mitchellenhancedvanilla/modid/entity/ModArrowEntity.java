package mitchellenhancedvanilla.modid.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

// Base for the mod's arrows: subclasses add stats and behaviour
public abstract class ModArrowEntity extends AbstractArrow {
    // Loading from save / client spawn
    protected ModArrowEntity(EntityType<? extends ModArrowEntity> type, Level level) {
        super(type, level);
        setBaseDamage(baseDamage());
    }

    // Fired by a bow or crossbow
    protected ModArrowEntity(EntityType<? extends ModArrowEntity> type, Level level, LivingEntity owner,
                             ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(type, owner, level, pickupItem, firedFromWeapon);
        setBaseDamage(baseDamage());
    }

    // Fired by a dispenser
    protected ModArrowEntity(EntityType<? extends ModArrowEntity> type, Level level, double x, double y, double z,
                             ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(type, x, y, z, level, pickupItem, firedFromWeapon);
        setBaseDamage(baseDamage());
    }

    // Vanilla arrows are 2.0
    protected double baseDamage() {
        return 2.0;
    }

    // The item this arrow drops when picked up
    protected abstract Item arrowItem();

    // Trail behind fully charged shots
    public SimpleParticleType critParticle() {
        return ParticleTypes.CRIT;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(arrowItem());
    }
}
