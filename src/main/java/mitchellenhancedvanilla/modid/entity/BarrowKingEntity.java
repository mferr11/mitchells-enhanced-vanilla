package mitchellenhancedvanilla.modid.entity;

import java.util.List;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.ModArmour;
import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.ModItems;
import mitchellenhancedvanilla.modid.entity.ai.BarrowKingCombatGoal;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

// First boss: an armoured skeleton warlord summoned with the Tarnished Crown
public class BarrowKingEntity extends Monster {
    // Iron bow at full draw is ~4.5 damage, so roughly 30-35 hits
    public static final double MAX_HEALTH = 100.0;
    // Base melee damage; the iron sword adds its own +5 on top
    public static final double ATTACK_DAMAGE = 2.0;
    public static final double ARMOR = 6.0;
    public static final double MOVEMENT_SPEED = 0.25;
    public static final double KNOCKBACK_RESISTANCE = 0.8;
    public static final double FOLLOW_RANGE = 40.0;
    public static final int XP_REWARD = 150;

    // Shield blocks arrows and melee within this many degrees of his facing
    public static final float SHIELD_HALF_ANGLE = 60.0F;
    // Melee attackers bounce off the raised shield
    public static final double SHIELD_BASH_KNOCKBACK = 0.6;

    // Sword reach past his hitbox (vanilla mobs ~0.83) (blocks)
    public static final double SWORD_REACH = 1.6;

    // Touching him hurts, like Terraria bosses; player i-frames pace it
    public static final float CONTACT_DAMAGE = 3.0F;

    // Rising out of / sinking back into the ground
    public static final int RISE_TICKS = 40;
    public static final double RISE_DEPTH = 2.6;
    // With no living player this close, he returns to his grave (blocks)
    public static final double CHALLENGER_RADIUS = 48.0;
    // How often the dawn and challenger checks run once he is up (ticks)
    public static final int DAWN_CHECK_INTERVAL = 20;

    // Phase 2 starts at this fraction of max health
    public static final float RETAINER_PHASE_HEALTH = 0.5F;
    // Retainers per wave, and the most he keeps up at once
    public static final int RETAINER_WAVE_SIZE = 3;
    public static final int MAX_RETAINERS = 6;
    // Retainers rise this far from him (blocks)
    public static final double RETAINER_MIN_DISTANCE = 3.0;
    public static final double RETAINER_MAX_DISTANCE = 7.0;
    // Retainers this close count towards his limit, and crumble when he goes (blocks)
    public static final double RETAINER_RANGE = 48.0;

    // Phase 3 starts at this fraction of max health
    public static final float ENRAGE_HEALTH = 0.25F;
    // Enraged movement (and charge) speed bonus, as a fraction of base speed
    public static final double ENRAGE_SPEED_BONUS = 0.3;
    private static final Identifier ENRAGE_SPEED_ID = MitchellEnhancedVanilla.id("barrow_king_enrage");

    // Health-driven fight phases
    public enum Phase { DUEL, RETAINERS, ENRAGED }

    // What the client needs to pose him
    public enum Pose { GUARD, WINDUP, STRIKE, BRACE, STUNNED }

    private enum Stage { RISING, FIGHTING, SINKING }

    private static final EntityDataAccessor<Byte> DATA_POSE =
        SynchedEntityData.defineId(BarrowKingEntity.class, EntityDataSerializers.BYTE);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
        Mth.createInsecureUUID(random), getDisplayName(), BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.NOTCHED_10);

    private Stage stage = Stage.FIGHTING;
    private Phase phase = Phase.DUEL;
    private int stageTicks;
    // Set in registerGoals, which runs during the super constructor (so no initializer)
    private @Nullable BarrowKingCombatGoal combatGoal;
    // Ground he is rising out of or sinking into
    private @Nullable BlockPos burrow;

    public BarrowKingEntity(EntityType<? extends BarrowKingEntity> type, Level level) {
        super(type, level);
        xpReward = XP_REWARD;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, MAX_HEALTH)
            .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
            .add(Attributes.ARMOR, ARMOR)
            .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
            .add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE)
            .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        combatGoal = new BarrowKingCombatGoal(this);
        goalSelector.addGoal(2, combatGoal);
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_POSE, (byte) Pose.GUARD.ordinal());
    }

    public Pose getKingPose() {
        return Pose.values()[entityData.get(DATA_POSE)];
    }

    public void setKingPose(Pose pose) {
        entityData.set(DATA_POSE, (byte) pose.ordinal());
    }

    // Shield is up unless he is mid-swing, winded, or has thrown it away (phase 3)
    public boolean isShieldRaised() {
        Pose pose = getKingPose();
        // Equipment syncs to clients, so this also works for the renderer
        return (pose == Pose.GUARD || pose == Pose.BRACE) && getOffhandItem().is(Items.SHIELD);
    }

    public boolean canFight() {
        return stage == Stage.FIGHTING;
    }

    public Phase getPhase() {
        return phase;
    }

    // Called by the crown: starts underground at the barrow and claws his way up
    public void beginRising(BlockPos ground) {
        burrow = ground;
        stage = Stage.RISING;
        stageTicks = 0;
        setPos(ground.getX() + 0.5, ground.getY() - RISE_DEPTH, ground.getZ() + 0.5);
        noPhysics = true;
        setNoGravity(true);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        // Shield must be in the off hand
        setLeftHanded(false);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        // His copy of the crown is wearable; the player's summon item is not
        ItemStack crown = new ItemStack(ModItems.TARNISHED_CROWN);
        crown.set(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setAsset(ModArmour.TARNISHED_CROWN_ASSET).build());
        setItemSlot(EquipmentSlot.HEAD, crown);
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        for (EquipmentSlot slot : EquipmentSlot.values()) setDropChance(slot, 0.0F);
        return data;
    }

    @Override
    public void tick() {
        // Keep the rise/sink moving through the ground
        if (stage != Stage.FIGHTING) {
            noPhysics = true;
            setDeltaMovement(Vec3.ZERO);
        }
        super.tick();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        stageTicks++;
        switch (stage) {
            case RISING -> tickBurrow(level, 1);
            case SINKING -> tickBurrow(level, -1);
            case FIGHTING -> tickFighting(level);
        }
        bossEvent.setProgress(getHealth() / getMaxHealth());
    }

    private void tickFighting(ServerLevel level) {
        if (stageTicks % DAWN_CHECK_INTERVAL == 0) {
            if (level.isBrightOutside()) {
                startSinking(level, "dawn");
                return;
            }
            if (!hasChallenger(level)) {
                startSinking(level, "victorious");
                return;
            }
        }
        if (!isAlive()) return;
        // Enrage is checked first so a single big hit skips phase 2 rather than announcing both
        if (phase != Phase.ENRAGED && getHealth() <= getMaxHealth() * ENRAGE_HEALTH) {
            enterEnragedPhase(level);
        } else if (phase == Phase.DUEL && getHealth() <= getMaxHealth() * RETAINER_PHASE_HEALTH) {
            enterRetainerPhase(level);
        }
        if (phase == Phase.ENRAGED && stageTicks % 4 == 0) {
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getEyeY(), getZ(), 1, 0.25, 0.15, 0.25, 0.01);
        }
        dealContactDamage(level);
    }

    // Phase 3: throws his shield aside and fights faster and harder
    private void enterEnragedPhase(ServerLevel level) {
        phase = Phase.ENRAGED;
        bossEvent.setColor(BossEvent.BossBarColor.RED);
        Vec3 shieldPos = getEyePosition().add(calculateViewVector(0.0F, getYHeadRot()).scale(0.6));
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.SHIELD), shieldPos.x, shieldPos.y - 0.6, shieldPos.z,
            24, 0.2, 0.2, 0.2, 0.2);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.SHIELD_BREAK.value(), SoundSource.HOSTILE, 1.5F, 0.6F);
        playSound(SoundEvents.RAVAGER_ROAR, 2.5F, 0.35F);
        setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        applyEnrageSpeed();
        broadcast(level, Component.translatable("entity.mitchells-enhanced-vanilla.barrow_king.enraged")
            .withStyle(ChatFormatting.RED));
        if (combatGoal != null) combatGoal.onEnraged();
    }

    private void applyEnrageSpeed() {
        AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && !speed.hasModifier(ENRAGE_SPEED_ID)) {
            speed.addPermanentModifier(new AttributeModifier(ENRAGE_SPEED_ID, ENRAGE_SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    // Phase 2: he calls his retainers up out of the barrow
    private void enterRetainerPhase(ServerLevel level) {
        phase = Phase.RETAINERS;
        bossEvent.setColor(BossEvent.BossBarColor.YELLOW);
        playSound(SoundEvents.RAVAGER_ROAR, 2.0F, 0.4F);
        broadcast(level, Component.translatable("entity.mitchells-enhanced-vanilla.barrow_king.retainers")
            .withStyle(ChatFormatting.YELLOW));
        if (combatGoal != null) combatGoal.raiseRetainersSoon();
    }

    private List<BarrowRetainerEntity> nearbyRetainers(ServerLevel level) {
        return level.getEntitiesOfClass(BarrowRetainerEntity.class, getBoundingBox().inflate(RETAINER_RANGE),
            BarrowRetainerEntity::isAlive);
    }

    public boolean canRaiseRetainers(ServerLevel level) {
        return phase == Phase.RETAINERS && nearbyRetainers(level).size() < MAX_RETAINERS;
    }

    // Raises a wave around him, up to his limit, aimed at his current target
    public void raiseRetainers(ServerLevel level) {
        int count = Math.min(RETAINER_WAVE_SIZE, MAX_RETAINERS - nearbyRetainers(level).size());
        for (int i = 0; i < count; i++) {
            BlockPos ground = findRetainerGround(level);
            if (ground == null) continue;
            BarrowRetainerEntity retainer = ModEntities.BARROW_RETAINER.create(level, EntitySpawnReason.MOB_SUMMONED);
            if (retainer == null) continue;
            retainer.finalizeSpawn(level, level.getCurrentDifficultyAt(ground), EntitySpawnReason.MOB_SUMMONED, null);
            retainer.beginRising(ground);
            retainer.setYRot(random.nextFloat() * 360.0F);
            retainer.setTarget(getTarget());
            level.addFreshEntity(retainer);
        }
        playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 2.0F, 0.6F);
    }

    // Open ground near him, roughly level with his feet
    private @Nullable BlockPos findRetainerGround(ServerLevel level) {
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dist = RETAINER_MIN_DISTANCE + random.nextDouble() * (RETAINER_MAX_DISTANCE - RETAINER_MIN_DISTANCE);
            int x = Mth.floor(getX() + Math.cos(angle) * dist);
            int z = Mth.floor(getZ() + Math.sin(angle) * dist);
            BlockPos ground = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            if (Math.abs(ground.getY() - getBlockY()) > 4) continue;
            if (!level.getFluidState(ground).isEmpty() || !level.getBlockState(ground.above()).isAir()) continue;
            return ground;
        }
        return null;
    }

    // His retainers fall apart once he is gone
    private void dismissRetainers(ServerLevel level) {
        for (BarrowRetainerEntity retainer : nearbyRetainers(level)) retainer.crumble(level);
    }

    private void dealContactDamage(ServerLevel level) {
        for (Player player : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(0.15),
                p -> p.isAlive() && !p.isSpectator())) {
            player.hurtServer(level, damageSources().mobAttack(this), CONTACT_DAMAGE);
        }
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        return getAttackBoundingBox(SWORD_REACH).intersects(target.getBoundingBox());
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel level) {
            dismissRetainers(level);
            broadcast(level, Component.translatable("entity.mitchells-enhanced-vanilla.barrow_king.defeated")
                .withStyle(ChatFormatting.GOLD));
        }
    }

    // Moves up (dir 1) or down (dir -1) through the ground, then finishes the stage
    private void tickBurrow(ServerLevel level, int dir) {
        setPos(getX(), getY() + dir * RISE_DEPTH / RISE_TICKS, getZ());
        groundBurst(level, 6);
        if (stageTicks % 8 == 0) playSound(SoundEvents.ROOTED_DIRT_BREAK, 1.5F, 0.6F);
        if (stageTicks < RISE_TICKS) return;

        if (dir < 0) {
            discard();
            return;
        }
        // Fully risen: snap onto the ground and start fighting
        stage = Stage.FIGHTING;
        stageTicks = 0;
        noPhysics = false;
        setNoGravity(false);
        if (burrow != null) setPos(getX(), burrow.getY(), getZ());
        playSound(SoundEvents.RAVAGER_ROAR, 2.0F, 0.5F);
    }

    // A living player is still nearby (creative counts so he can be tested; spectators don't)
    private boolean hasChallenger(ServerLevel level) {
        return level.players().stream().anyMatch(p -> p.isAlive() && !p.isSpectator()
            && p.distanceToSqr(this) < CHALLENGER_RADIUS * CHALLENGER_RADIUS);
    }

    // Burrows back down; reason picks the lang key suffix
    private void startSinking(ServerLevel level, String reason) {
        stage = Stage.SINKING;
        stageTicks = 0;
        setTarget(null);
        noPhysics = true;
        setNoGravity(true);
        getNavigation().stop();
        // Sinks where he stands
        burrow = blockPosition();
        dismissRetainers(level);
        playSound(SoundEvents.WITHER_SKELETON_AMBIENT, 2.0F, 0.5F);
        broadcast(level, Component.translatable("entity.mitchells-enhanced-vanilla.barrow_king." + reason));
    }

    // Dirt sprays from the ground he is moving through
    private void groundBurst(ServerLevel level, int count) {
        BlockPos below = burrow != null ? burrow.below() : blockPosition().below();
        BlockState ground = level.getBlockState(below);
        if (ground.isAir()) return;
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
            getX(), below.getY() + 1.0, getZ(), count, 0.6, 0.1, 0.6, 0.15);
    }

    private void broadcast(ServerLevel level, Component message) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) < 96 * 96) player.sendSystemMessage(message);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        // Untouchable while in the ground
        if (stage != Stage.FIGHTING && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        if (blocksWithShield(source)) {
            Vec3 spark = getEyePosition().add(calculateViewVector(0.0F, getYHeadRot()).scale(0.6));
            LivingEntity meleeAttacker = !source.is(DamageTypeTags.IS_PROJECTILE) && source.getDirectEntity() instanceof LivingEntity l ? l : null;
            // Axes knock his guard open instead of glancing off
            if (meleeAttacker != null && meleeAttacker.getSecondsToDisableBlocking() > 0.0F && combatGoal != null) {
                combatGoal.breakGuard();
                level.playSound(null, getX(), getY(), getZ(), SoundEvents.SHIELD_BREAK.value(), SoundSource.HOSTILE, 1.0F, 0.8F + random.nextFloat() * 0.4F);
                level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.SHIELD), spark.x, spark.y - 0.6, spark.z, 16, 0.15, 0.15, 0.15, 0.15);
                return false;
            }
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.HOSTILE, 1.0F, 0.8F + random.nextFloat() * 0.4F);
            level.sendParticles(ParticleTypes.CRIT, spark.x, spark.y - 0.6, spark.z, 8, 0.1, 0.1, 0.1, 0.3);
            // Melee attackers get shoved back
            if (meleeAttacker != null) {
                LivingEntity attacker = meleeAttacker;
                attacker.knockback(SHIELD_BASH_KNOCKBACK, getX() - attacker.getX(), getZ() - attacker.getZ(), source, 0.0F);
                // Players move client-side, so they must be told about the shove directly
                if (attacker instanceof ServerPlayer player) player.connection.send(new ClientboundSetEntityMotionPacket(player));
            }
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    // Arrows and melee from the front hit the shield; piercing arrows punch through like vanilla
    private boolean blocksWithShield(DamageSource source) {
        if (!isShieldRaised()) return false;
        boolean melee = source.getDirectEntity() instanceof LivingEntity;
        if (!melee && !source.is(DamageTypeTags.IS_PROJECTILE)) return false;
        if (source.getDirectEntity() instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0) return false;
        Vec3 from = source.getSourcePosition();
        if (from == null) return false;
        Vec3 facing = calculateViewVector(0.0F, getYHeadRot());
        Vec3 toSource = new Vec3(from.x - getX(), 0.0, from.z - getZ()).normalize();
        return toSource.dot(facing) > Math.cos(Math.toRadians(SHIELD_HALF_ANGLE));
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        bossEvent.setName(getDisplayName());
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.7F;
    }

    private static final String HOME_KEY = "BarrowHome";
    private static final String STAGE_KEY = "BarrowStage";
    private static final String STAGE_TICKS_KEY = "BarrowStageTicks";
    private static final String PHASE_KEY = "BarrowPhase";

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (burrow != null) output.store(HOME_KEY, BlockPos.CODEC, burrow);
        output.putInt(STAGE_KEY, stage.ordinal());
        output.putInt(PHASE_KEY, phase.ordinal());
        output.putInt(STAGE_TICKS_KEY, stageTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        burrow = input.read(HOME_KEY, BlockPos.CODEC).orElse(null);
        stage = Stage.values()[Mth.clamp(input.getIntOr(STAGE_KEY, 1), 0, Stage.values().length - 1)];
        stageTicks = input.getIntOr(STAGE_TICKS_KEY, 0);
        phase = Phase.values()[Mth.clamp(input.getIntOr(PHASE_KEY, 0), 0, Phase.values().length - 1)];
        if (phase == Phase.RETAINERS) bossEvent.setColor(BossEvent.BossBarColor.YELLOW);
        if (phase == Phase.ENRAGED) {
            bossEvent.setColor(BossEvent.BossBarColor.RED);
            applyEnrageSpeed();
        }
        if (stage != Stage.FIGHTING) {
            noPhysics = true;
            setNoGravity(true);
        }
        if (hasCustomName()) bossEvent.setName(getDisplayName());
    }
}
