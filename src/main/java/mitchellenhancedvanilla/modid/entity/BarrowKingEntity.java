package mitchellenhancedvanilla.modid.entity;

import java.util.List;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.ModArmour;
import mitchellenhancedvanilla.modid.ModEntities;
import mitchellenhancedvanilla.modid.ScreenShakePayload;
import mitchellenhancedvanilla.modid.ModItems;
import mitchellenhancedvanilla.modid.entity.ai.BarrowKingCombatGoal;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
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
    public static final double MAX_HEALTH = 150.0;
    // Base melee damage; the iron sword adds its own +5 on top
    public static final double ATTACK_DAMAGE = 1.0;
    public static final double ARMOR = 6.0;
    public static final double MOVEMENT_SPEED = 0.25;
    public static final double KNOCKBACK_RESISTANCE = 0.8;
    public static final double FOLLOW_RANGE = 40.0;
    public static final int XP_REWARD = 300;

    // Shield blocks arrows and melee within this many degrees of his facing
    public static final float SHIELD_HALF_ANGLE = 60.0F;
    // He holds the real Barrow King's Shield, but with his own model id so it shows his texture, not the player one
    public static final Identifier SHIELD_MODEL = MitchellEnhancedVanilla.id("kings_shield_king");
    // Melee attackers bounce off the raised shield
    public static final double SHIELD_BASH_KNOCKBACK = 0.5;

    // Sword reach past his hitbox (vanilla mobs ~0.83) (blocks)
    public static final double SWORD_REACH = 1.6;

    // Touching him hurts, like Terraria bosses; player i-frames pace it
    public static final float CONTACT_DAMAGE = 3.0F;

    // Rising out of / sinking back into the ground
    public static final int RISE_TICKS = 40;
    public static final double RISE_DEPTH = 2.6;
    // Camera shake while he rises: peak strength (degrees) and reach (blocks)
    public static final float RISE_SHAKE_STRENGTH = 0.6F;
    public static final double RISE_SHAKE_RADIUS = 48.0;
    // Players this close get the emerge sound cut off (it carries 32 blocks at the crown's volume)
    public static final double EMERGE_SOUND_RANGE = 48.0;

    // Bone particles when hurt: base count, extra per point of damage, cap
    public static final int BONE_PARTICLES_BASE = 12;
    public static final float BONE_PARTICLES_PER_DAMAGE = 1.5F;
    public static final int BONE_PARTICLES_MAX = 36;

    // Short camera shake with each roar: strength at volume 2.0 (degrees, scales with volume), length, reach (blocks)
    public static final float ROAR_SHAKE_STRENGTH = 0.8F;
    public static final int ROAR_SHAKE_TICKS = 12;

    // Soul wisps: burst on death, seeping from the ground every other tick while rising or sinking,
    // and trailing behind him when enraged and moving at least this fast (blocks per tick)
    public static final int DEATH_SOULS = 45;
    public static final int BURROW_SOULS = 3;
    public static final double ENRAGED_TRAIL_MIN_SPEED = 0.08;
    public static final double ROAR_SHAKE_RADIUS = 32.0;
    // Granted to every player within his follow range when he dies
    private static final Identifier DEFEAT_ADVANCEMENT = MitchellEnhancedVanilla.id("adventure/defeat_barrow_king");
    private static final String DEFEAT_CRITERION = "defeated";

    // With no living player this close, he returns to his grave (blocks)
    public static final double CHALLENGER_RADIUS = 48.0;
    // How often the dawn and challenger checks run once he is up (ticks)
    public static final int DAWN_CHECK_INTERVAL = 20;

    // Chat colour for his phase changes and departures; matches the crown's "has awoken" message
    public static final ChatFormatting MESSAGE_COLOUR = ChatFormatting.DARK_PURPLE;

    // Phase 2 starts at this fraction of max health
    public static final float SERVANT_PHASE_HEALTH = 0.5F;
    // Servants per wave, and the most he keeps up at once
    public static final int SERVANT_WAVE_SIZE = 3;
    public static final int MAX_SERVANTS = 6;
    // Servants rise this far from him (blocks)
    public static final double SERVANT_MIN_DISTANCE = 3.0;
    public static final double SERVANT_MAX_DISTANCE = 7.0;
    // Servants this close count towards his limit, and crumble when he goes (blocks)
    public static final double SERVANT_RANGE = 48.0;

    // Phase 3 starts at this fraction of max health
    public static final float ENRAGE_HEALTH = 0.25F;
    // Enraged movement (and charge) speed bonus, as a fraction of base speed
    public static final double ENRAGE_SPEED_BONUS = 0.2;
    private static final Identifier ENRAGE_SPEED_ID = MitchellEnhancedVanilla.id("barrow_king_enrage");

    // Health-driven fight phases
    public enum Phase { DUEL, SERVANTS, ENRAGED }

    // What the client needs to pose him
    // STUNNED is the brief shield-down recovery; DAZED is a real stun (wall, missed charge, guard break)
    public enum Pose { GUARD, WINDUP, STRIKE, BRACE, STUNNED, SUMMON, DAZED }

    private enum Stage { RISING, FIGHTING, SINKING }

    private static final EntityDataAccessor<Byte> DATA_POSE =
        SynchedEntityData.defineId(BarrowKingEntity.class, EntityDataSerializers.BYTE);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
        Mth.createInsecureUUID(random), getDisplayName(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);

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
        return (pose == Pose.GUARD || pose == Pose.BRACE) && getOffhandItem().has(DataComponents.BLOCKS_ATTACKS);
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
        setItemSlot(EquipmentSlot.OFFHAND, kingsShield());
        // His copy of the crown is wearable; the player's summon item is not
        ItemStack crown = new ItemStack(ModItems.TARNISHED_CROWN);
        crown.set(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setAsset(ModArmour.TARNISHED_CROWN_ASSET).build());
        setItemSlot(EquipmentSlot.HEAD, crown);
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        for (EquipmentSlot slot : EquipmentSlot.values()) setDropChance(slot, 0.0F);
        return data;
    }

    // The Barrow King's Shield, shown with his weathered texture rather than the clean player one
    private static ItemStack kingsShield() {
        ItemStack shield = new ItemStack(ModItems.KINGS_SHIELD);
        shield.set(DataComponents.ITEM_MODEL, SHIELD_MODEL);
        return shield;
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
        } else if (phase == Phase.DUEL && getHealth() <= getMaxHealth() * SERVANT_PHASE_HEALTH) {
            enterServantPhase(level);
        }
        if (phase == Phase.ENRAGED && stageTicks % 4 == 0) {
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getEyeY(), getZ(), 1, 0.25, 0.15, 0.25, 0.01);
        }
        if (phase == Phase.ENRAGED) enragedTrail(level);
        dealContactDamage(level);
    }

    // Faint wisps behind him whenever he moves while enraged
    private void enragedTrail(ServerLevel level) {
        Vec3 motion = getDeltaMovement().multiply(1.0, 0.0, 1.0);
        double speed = motion.length();
        if (speed < ENRAGED_TRAIL_MIN_SPEED) return;
        soulStreak(level, position(), motion.scale(-1.0 / speed), speed, 1, getY(0.55));
    }

    // Soul wisps laid along a line starting at `from` and running `length` blocks in `dir` (flat, normalised)
    public static void soulStreak(ServerLevel level, Vec3 from, Vec3 dir, double length, int points, double y) {
        for (int i = 0; i < points; i++) {
            Vec3 at = from.add(dir.scale(0.4 + length * i / points));
            level.sendParticles(ParticleTypes.SOUL, at.x, y, at.z, 1, 0.12, 0.3, 0.12, 0.01);
        }
    }

    // Phase 3: throws his shield aside and fights faster and harder
    private void enterEnragedPhase(ServerLevel level) {
        phase = Phase.ENRAGED;
        Vec3 shieldPos = getEyePosition().add(calculateViewVector(0.0F, getYHeadRot()).scale(0.6));
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.SHIELD), shieldPos.x, shieldPos.y - 0.6, shieldPos.z,
            24, 0.2, 0.2, 0.2, 0.2);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.SHIELD_BREAK.value(), SoundSource.HOSTILE, 1.5F, 0.6F);
        roar(level, 2.5F, 0.35F);
        setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        applyEnrageSpeed();
        broadcast(level, Component.translatable("entity.mitchells-enhanced-vanilla.barrow_king.enraged")
            .withStyle(MESSAGE_COLOUR));
        if (combatGoal != null) combatGoal.onEnraged();
    }

    private void applyEnrageSpeed() {
        AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && !speed.hasModifier(ENRAGE_SPEED_ID)) {
            speed.addPermanentModifier(new AttributeModifier(ENRAGE_SPEED_ID, ENRAGE_SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    // Phase 2: he calls his servants up out of the barrow
    private void enterServantPhase(ServerLevel level) {
        phase = Phase.SERVANTS;
        roar(level, 2.0F, 0.4F);
        broadcast(level, Component.translatable("entity.mitchells-enhanced-vanilla.barrow_king.servants")
            .withStyle(MESSAGE_COLOUR));
        if (combatGoal != null) combatGoal.raiseServantsSoon();
    }

    private List<BarrowServantEntity> nearbyServants(ServerLevel level) {
        return level.getEntitiesOfClass(BarrowServantEntity.class, getBoundingBox().inflate(SERVANT_RANGE),
            BarrowServantEntity::isAlive);
    }

    public boolean canRaiseServants(ServerLevel level) {
        // Phase 2 onwards, including while enraged
        return phase != Phase.DUEL && nearbyServants(level).size() < MAX_SERVANTS;
    }

    // Raises a wave around him, up to his limit, aimed at his current target
    public void raiseServants(ServerLevel level) {
        int count = Math.min(SERVANT_WAVE_SIZE, MAX_SERVANTS - nearbyServants(level).size());
        for (int i = 0; i < count; i++) {
            BlockPos ground = findServantGround(level);
            if (ground == null) continue;
            BarrowServantEntity servant = ModEntities.BARROW_SERVANT.create(level, EntitySpawnReason.MOB_SUMMONED);
            if (servant == null) continue;
            servant.finalizeSpawn(level, level.getCurrentDifficultyAt(ground), EntitySpawnReason.MOB_SUMMONED, null);
            servant.beginRising(ground);
            servant.setYRot(random.nextFloat() * 360.0F);
            servant.setTarget(getTarget());
            level.addFreshEntity(servant);
        }
        playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 2.0F, 0.6F);
    }

    // Open ground near him, roughly level with his feet
    private @Nullable BlockPos findServantGround(ServerLevel level) {
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dist = SERVANT_MIN_DISTANCE + random.nextDouble() * (SERVANT_MAX_DISTANCE - SERVANT_MIN_DISTANCE);
            int x = Mth.floor(getX() + Math.cos(angle) * dist);
            int z = Mth.floor(getZ() + Math.sin(angle) * dist);
            BlockPos ground = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            if (Math.abs(ground.getY() - getBlockY()) > 4) continue;
            if (!level.getFluidState(ground).isEmpty() || !level.getBlockState(ground.above()).isAir()) continue;
            return ground;
        }
        return null;
    }

    // His servants fall apart once he is gone
    private void dismissServants(ServerLevel level) {
        for (BarrowServantEntity servant : nearbyServants(level)) servant.crumble(level);
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
            // The barrow's bound spirits pour out of him
            level.sendParticles(ParticleTypes.SOUL, getX(), getY(0.5), getZ(), DEATH_SOULS, 0.4, 0.9, 0.4, 0.06);
            dismissServants(level);
            broadcast(level, Component.translatable("entity.mitchells-enhanced-vanilla.barrow_king.defeated")
                .withStyle(ChatFormatting.GOLD));
            // Same test vanilla uses for player kills, so /kill or lava doesn't count
            if (getLastHurtByPlayer() != null) awardDefeat(level);
        }
    }

    // Everyone he could have been fighting shares the kill, not just whoever landed the last hit
    private void awardDefeat(ServerLevel level) {
        AdvancementHolder advancement = level.getServer().getAdvancements().get(DEFEAT_ADVANCEMENT);
        if (advancement == null) return;
        double range = getAttributeValue(Attributes.FOLLOW_RANGE);
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator() && player.distanceToSqr(this) <= range * range) {
                player.getAdvancements().award(advancement, DEFEAT_CRITERION);
            }
        }
    }

    // Moves up (dir 1) or down (dir -1) through the ground, then finishes the stage
    private void tickBurrow(ServerLevel level, int dir) {
        // The ground heaves as he claws his way up
        if (dir > 0 && stageTicks == 1) {
            ScreenShakePayload.shakeNearby(level, position(), RISE_SHAKE_RADIUS, RISE_SHAKE_STRENGTH, RISE_TICKS);
        }
        setPos(getX(), getY() + dir * RISE_DEPTH / RISE_TICKS, getZ());
        groundBurst(level, 6);
        // Souls seep up out of the grave around him
        BlockPos grave = burrow;
        if (stageTicks % 2 == 0 && grave != null) {
            level.sendParticles(ParticleTypes.SOUL, grave.getX() + 0.5, grave.getY() + 0.2, grave.getZ() + 0.5,
                BURROW_SOULS, 0.8, 0.1, 0.8, 0.02);
        }
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
        stopEmergeSound(level);
        roar(level, 2.0F, 0.5F);
    }

    // The crown's emerge sound runs ~12s; cut it as he finishes rising (the roar covers the cut)
    // Ravager roar plus a short shake; louder roars shake harder
    public void roar(ServerLevel level, float volume, float pitch) {
        playSound(SoundEvents.RAVAGER_ROAR, volume, pitch);
        ScreenShakePayload.shakeNearby(level, position(), ROAR_SHAKE_RADIUS, ROAR_SHAKE_STRENGTH * volume / 2.0F, ROAR_SHAKE_TICKS);
    }

    private void stopEmergeSound(ServerLevel level) {
        ClientboundStopSoundPacket stop = new ClientboundStopSoundPacket(SoundEvents.WARDEN_EMERGE.location(), SoundSource.HOSTILE);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) < EMERGE_SOUND_RANGE * EMERGE_SOUND_RANGE) player.connection.send(stop);
        }
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
        dismissServants(level);
        playSound(SoundEvents.WITHER_SKELETON_AMBIENT, 2.0F, 0.5F);
        broadcast(level, Component.translatable("entity.mitchells-enhanced-vanilla.barrow_king." + reason).withStyle(MESSAGE_COLOUR));
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
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt) boneBurst(level, source, damage);
        return hurt;
    }

    // Bone chips fly off where he was hit; bigger hits throw more
    private void boneBurst(ServerLevel level, DamageSource source, float damage) {
        Vec3 from = source.getSourcePosition();
        Vec3 centre = getBoundingBox().getCenter();
        // Towards the attacker's side of his body, or the middle for sourceless damage
        Vec3 at = from == null ? centre : centre.add(new Vec3(from.x - centre.x, 0.0, from.z - centre.z).normalize().scale(getBbWidth() * 0.5));
        int count = Mth.clamp(BONE_PARTICLES_BASE + Math.round(damage * BONE_PARTICLES_PER_DAMAGE), 1, BONE_PARTICLES_MAX);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.BONE), at.x, at.y, at.z, count, 0.2, 0.4, 0.2, 0.12);
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
        if (phase == Phase.ENRAGED) applyEnrageSpeed();
        if (stage != Stage.FIGHTING) {
            noPhysics = true;
            setNoGravity(true);
        }
        if (hasCustomName()) bossEvent.setName(getDisplayName());
        // Kings saved by older versions carry a shield with an outdated look; swap in the current one
        ItemStack offhand = getOffhandItem();
        if (offhand.has(DataComponents.BLOCKS_ATTACKS) && !SHIELD_MODEL.equals(offhand.get(DataComponents.ITEM_MODEL))) {
            setItemSlot(EquipmentSlot.OFFHAND, kingsShield());
        }
    }
}
