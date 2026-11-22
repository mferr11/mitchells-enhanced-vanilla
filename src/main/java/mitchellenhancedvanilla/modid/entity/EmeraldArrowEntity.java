package mitchellenhancedvanilla.modid.entity;

import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.ModItems;
import mitchellenhancedvanilla.modid.ModParticles;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class EmeraldArrowEntity extends ModArrowEntity {
    // Vanilla arrows have a base damage of 2.0
    public static final double BASE_DAMAGE = 3.0;

    // Kills count as at least this Looting level (III is the enchantment's max)
    public static final int LOOTING_LEVEL = 3;

    // Set while a mob killed by an Emerald Arrow drops its loot (read by EnchantmentHelperMixin)
    public static boolean droppingLoot;

    public EmeraldArrowEntity(EntityType<? extends EmeraldArrowEntity> type, Level level) {
        super(type, level);
    }

    public EmeraldArrowEntity(Level level, LivingEntity owner, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.EMERALD_ARROW, level, owner, pickupItem, firedFromWeapon);
    }

    public EmeraldArrowEntity(Level level, double x, double y, double z, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.EMERALD_ARROW, level, x, y, z, pickupItem, firedFromWeapon);
    }

    @Override
    protected double baseDamage() {
        return BASE_DAMAGE;
    }

    @Override
    protected Item arrowItem() {
        return ModItems.EMERALD_ARROW;
    }

    @Override
    public SimpleParticleType critParticle() {
        return ModParticles.EMERALD_CRIT;
    }
}
