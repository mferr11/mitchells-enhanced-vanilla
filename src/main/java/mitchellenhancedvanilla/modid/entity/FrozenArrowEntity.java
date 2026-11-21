package mitchellenhancedvanilla.modid.entity;

import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.ModItems;
import mitchellenhancedvanilla.modid.ModParticles;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class FrozenArrowEntity extends ModArrowEntity {
    // Vanilla arrows have a base damage of 2.0
    public static final double BASE_DAMAGE = 3.0;

    // How long a hit target stays fully frozen (slowed and taking freeze damage) in ticks
    public static final int FULLY_FROZEN_TICKS = 6 * 20;

    // Extra Slowness while fully frozen, on top of vanilla's frost slowdown
    public static final int SLOWNESS_AMPLIFIER = 0;

    public FrozenArrowEntity(EntityType<? extends FrozenArrowEntity> type, Level level) {
        super(type, level);
    }

    public FrozenArrowEntity(Level level, LivingEntity owner, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.FROZEN_ARROW, level, owner, pickupItem, firedFromWeapon);
    }

    public FrozenArrowEntity(Level level, double x, double y, double z, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.FROZEN_ARROW, level, x, y, z, pickupItem, firedFromWeapon);
    }

    @Override
    protected double baseDamage() {
        return BASE_DAMAGE;
    }

    @Override
    protected Item arrowItem() {
        return ModItems.FROZEN_ARROW;
    }

    @Override
    public SimpleParticleType critParticle() {
        return ModParticles.FROZEN_CRIT;
    }

    // Freezes like powdered snow; freeze-immune targets are unaffected
    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        if (!target.canFreeze()) return;

        int frozenTicks = target.getTicksRequiredToFreeze() + 2 * FULLY_FROZEN_TICKS;
        // Don't shorten an existing freeze
        target.setTicksFrozen(Math.max(target.getTicksFrozen(), frozenTicks));

        // Slowness for the same duration
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, FULLY_FROZEN_TICKS, SLOWNESS_AMPLIFIER,
            false, false, true), getEffectSource());
    }
}
