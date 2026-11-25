package mitchellenhancedvanilla.modid.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

// Skeleton guard raised by the Barrow King in phase 2: weaker than a vanilla skeleton
public class BarrowServantEntity extends AbstractSkeleton {
    // Vanilla skeletons have 20 health
    public static final double MAX_HEALTH = 5.0;
    // Fists; a wooden sword adds its own +3
    public static final double ATTACK_DAMAGE = 1.0;
    public static final float WOODEN_SWORD_CHANCE = 0.5F;
    // Shambling: vanilla skeletons are 0.25, zombies 0.23
    public static final double MOVEMENT_SPEED = 0.23;
    public static final int XP_REWARD = 1;

    // Soul wisps released when killed or crumbled
    public static final int DEATH_SOULS = 3;

    // Claws up out of the ground over this long
    public static final int RISE_TICKS = 60;
    public static final double RISE_DEPTH = 2.1;

    // Ticks left rising; 0 once up
    private int riseTicks;
    private int groundY;

    public BarrowServantEntity(EntityType<? extends BarrowServantEntity> type, Level level) {
        super(type, level);
        xpReward = XP_REWARD;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSkeleton.createAttributes()
            .add(Attributes.MAX_HEALTH, MAX_HEALTH)
            .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
            .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        setItemSlot(EquipmentSlot.MAINHAND, random.nextFloat() < WOODEN_SWORD_CHANCE ? new ItemStack(Items.WOODEN_SWORD) : ItemStack.EMPTY);
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
    }

    // No enchanted gear
    @Override
    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor level, RandomSource random, DifficultyInstance difficulty) {
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        // Undo vanilla's extras (loot pickup, Halloween pumpkins)
        setCanPickUpLoot(false);
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
        for (EquipmentSlot slot : EquipmentSlot.values()) setDropChance(slot, 0.0F);
        // Graveyard spawns claw their way up too; the King's summons start rising separately
        if (reason == EntitySpawnReason.NATURAL) beginRising(blockPosition());
        return data;
    }

    // Starts underground at the given ground block and rises out of it
    public void beginRising(BlockPos ground) {
        groundY = ground.getY();
        riseTicks = RISE_TICKS;
        setPos(ground.getX() + 0.5, groundY - RISE_DEPTH, ground.getZ() + 0.5);
        noPhysics = true;
        setNoGravity(true);
    }

    public boolean isRising() {
        return riseTicks > 0;
    }

    // No walking until fully out of the ground
    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isRising();
    }

    @Override
    public void tick() {
        if (isRising()) {
            noPhysics = true;
            setDeltaMovement(Vec3.ZERO);
        }
        super.tick();
        // Not in the AI step: vanilla skips that entirely while immobile
        if (isRising() && level() instanceof ServerLevel level) tickRise(level);
    }

    private void tickRise(ServerLevel level) {
        setPos(getX(), getY() + RISE_DEPTH / RISE_TICKS, getZ());
        // Dirt every other tick (same look, half the packets)
        if (riseTicks % 2 == 0) {
            BarrowKingEntity.groundDust(level, BlockPos.containing(getX(), groundY - 1, getZ()), getX(), getZ(), 8, 0.3, 0.1);
        }
        if (--riseTicks > 0) return;
        noPhysics = false;
        setNoGravity(false);
        setPos(getX(), groundY, getZ());
    }

    // Collapses into bones: used when the King falls or leaves
    public void crumble(ServerLevel level) {
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.BONE),
            getX(), getY(0.5), getZ(), 12, 0.2, 0.4, 0.2, 0.08);
        releaseSoul(level);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.SKELETON_DEATH, SoundSource.HOSTILE, 0.8F, 0.8F);
        discard();
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel level) releaseSoul(level);
    }

    // The bound spirit slips free
    private void releaseSoul(ServerLevel level) {
        level.sendParticles(ParticleTypes.SOUL, getX(), getY(0.6), getZ(), DEATH_SOULS, 0.15, 0.3, 0.15, 0.03);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        // Untouchable while still in the ground
        if (isRising() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        return super.hurtServer(level, source, damage);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.SKELETON_STEP;
    }

    private static final String RISE_TICKS_KEY = "RiseTicks";
    private static final String GROUND_Y_KEY = "RiseGroundY";

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(RISE_TICKS_KEY, riseTicks);
        output.putInt(GROUND_Y_KEY, groundY);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        riseTicks = input.getIntOr(RISE_TICKS_KEY, 0);
        groundY = input.getIntOr(GROUND_Y_KEY, 0);
        if (isRising()) {
            noPhysics = true;
            setNoGravity(true);
        }
    }
}
