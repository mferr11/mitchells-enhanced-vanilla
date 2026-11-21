package mitchellenhancedvanilla.modid.entity;

import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.ModItems;
import mitchellenhancedvanilla.modid.ModParticles;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class GoldArrowEntity extends ModArrowEntity {
    // Vanilla arrows have a base damage of 2.0
    public static final double BASE_DAMAGE = 4.0;

    // Kills count as at least this Looting level
    public static final int LOOTING_LEVEL = 2;

    // How long a hit piglin admires the arrow before fighting back (ticks)
    public static final int PIGLIN_ADMIRE_TICKS = 3 * 20;

    // Disintegration burst: particle count and speed (amethyst is 48 at 0.48)
    public static final int BURST_PARTICLES = 20;
    public static final double BURST_SPEED = 0.3;

    // Set while a mob killed by a Gold Arrow drops its loot (read by EnchantmentHelperMixin)
    public static boolean droppingLoot;

    public GoldArrowEntity(EntityType<? extends GoldArrowEntity> type, Level level) {
        super(type, level);
    }

    public GoldArrowEntity(Level level, LivingEntity owner, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.GOLD_ARROW, level, owner, pickupItem, firedFromWeapon);
    }

    public GoldArrowEntity(Level level, double x, double y, double z, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.GOLD_ARROW, level, x, y, z, pickupItem, firedFromWeapon);
    }

    @Override
    protected double baseDamage() {
        return BASE_DAMAGE;
    }

    @Override
    protected Item arrowItem() {
        return ModItems.GOLD_ARROW;
    }

    @Override
    public SimpleParticleType critParticle() {
        return ModParticles.GOLD_CRIT;
    }

    // Piglins stop to admire the arrow, then fight (the hit already angered them)
    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        if (!(target instanceof Piglin piglin) || !piglin.isAlive() || !piglin.getOffhandItem().isEmpty()) return;

        // Vanilla blocks admiring after a player hit; lift that for this one arrow
        piglin.getBrain().eraseMemory(MemoryModuleType.ADMIRING_DISABLED);
        piglin.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.GOLD_ARROW));
        piglin.setGuaranteedDrop(EquipmentSlot.OFFHAND);
        piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, PIGLIN_ADMIRE_TICKS);
    }

    // Disintegrates on blocks instead of sticking
    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level() instanceof ServerLevel level) disintegrate(level);
    }

    // Harmless puff of gold
    private void disintegrate(ServerLevel level) {
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.GOLD_NUGGET),
            getX(), getY(), getZ(), BURST_PARTICLES, 0.1, 0.1, 0.1, BURST_SPEED);
        // Gold sparks for a bit of flash
        level.sendParticles(ModParticles.GOLD_CRIT, getX(), getY(), getZ(), BURST_PARTICLES / 2, 0.1, 0.1, 0.1, BURST_SPEED * 1.5);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.CHAIN_BREAK, SoundSource.NEUTRAL, 0.8F, 1.6F);
        discard();
    }
}
