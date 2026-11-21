package mitchellenhancedvanilla.modid.entity;

import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.ModItems;
import mitchellenhancedvanilla.modid.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jspecify.annotations.Nullable;

public class PrismarineArrowEntity extends ModArrowEntity {
    // Vanilla arrows have a base damage of 2.0
    public static final double BASE_DAMAGE = 3.0;

    // Damage multiplier vs mobs Impaling affects (drowned, guardians, squid, fish...)
    public static final double AQUATIC_DAMAGE_MULTIPLIER = 2.0;

    // Speed kept per tick in water (vanilla arrows 0.6, tridents 0.99)
    public static final float WATER_INERTIA = 0.99F;

    // Bubbles per tick while travelling through water
    public static final int BUBBLES_PER_TICK = 2;

    public PrismarineArrowEntity(EntityType<? extends PrismarineArrowEntity> type, Level level) {
        super(type, level);
    }

    public PrismarineArrowEntity(Level level, LivingEntity owner, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.PRISMARINE_ARROW, level, owner, pickupItem, firedFromWeapon);
    }

    public PrismarineArrowEntity(Level level, double x, double y, double z, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.PRISMARINE_ARROW, level, x, y, z, pickupItem, firedFromWeapon);
    }

    @Override
    protected double baseDamage() {
        return BASE_DAMAGE;
    }

    @Override
    protected Item arrowItem() {
        return ModItems.PRISMARINE_ARROW;
    }

    @Override
    public SimpleParticleType critParticle() {
        return ModParticles.PRISMARINE_CRIT;
    }

    // Keeps its speed underwater like a trident
    @Override
    protected float getWaterInertia() {
        return WATER_INERTIA;
    }

    // Boost damage only for this hit on aquatic mobs
    @Override
    protected void onHitEntity(EntityHitResult result) {
        boolean aquatic = result.getEntity().typeHolder().is(EntityTypeTags.SENSITIVE_TO_IMPALING);
        if (aquatic) setBaseDamage(baseDamage() * AQUATIC_DAMAGE_MULTIPLIER);
        super.onHitEntity(result);
        if (aquatic) setBaseDamage(baseDamage());
    }

    @Override
    public void tick() {
        super.tick();
        // Bubble trail, client side only
        if (level().isClientSide() && isInWater() && !isInGround()) {
            for (int i = 0; i < BUBBLES_PER_TICK; i++) {
                level().addParticle(ParticleTypes.BUBBLE, getRandomX(0.2), getRandomY(), getRandomZ(0.2), 0.0, 0.05, 0.0);
            }
        }
    }
}
