package mitchellenhancedvanilla.modid.entity;

import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.ModItems;
import mitchellenhancedvanilla.modid.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CopperArrowEntity extends ModArrowEntity {
    // Chance of lightning on impact during a thunderstorm
    public static final float LIGHTNING_CHANCE = 0.5F;

    // Vanilla arrows have a base damage of 2.0
    public static final double BASE_DAMAGE = 3.0;

    // Only roll once, even if it hits a mob then a block
    private boolean lightningRolled;

    public CopperArrowEntity(EntityType<? extends CopperArrowEntity> type, Level level) {
        super(type, level);
    }

    public CopperArrowEntity(Level level, LivingEntity owner, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.COPPER_ARROW, level, owner, pickupItem, firedFromWeapon);
    }

    public CopperArrowEntity(Level level, double x, double y, double z, ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.COPPER_ARROW, level, x, y, z, pickupItem, firedFromWeapon);
    }

    @Override
    protected double baseDamage() {
        return BASE_DAMAGE;
    }

    @Override
    protected Item arrowItem() {
        return ModItems.COPPER_ARROW;
    }

    @Override
    public SimpleParticleType critParticle() {
        return ModParticles.COPPER_CRIT;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        tryLightning(result.getEntity().blockPosition());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        // The air block on the side that was hit
        tryLightning(result.getBlockPos().relative(result.getDirection()));
    }

    // Like Channeling: needs a thunderstorm and open sky
    private void tryLightning(BlockPos pos) {
        if (lightningRolled || !(level() instanceof ServerLevel level)) return;
        lightningRolled = true;
        if (!level.isThundering() || !level.canSeeSky(pos) || random.nextFloat() >= LIGHTNING_CHANCE) return;

        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt == null) return;
        bolt.snapTo(Vec3.atBottomCenterOf(pos));
        // Credits kills to the shooter
        if (getOwner() instanceof ServerPlayer player) bolt.setCause(player);
        level.addFreshEntity(bolt);
        level.playSound(null, pos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.WEATHER, 5.0F, 1.0F);
    }
}
