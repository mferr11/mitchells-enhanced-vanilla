package mitchellenhancedvanilla.modid.entity;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import mitchellenhancedvanilla.modid.ModBlocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

// Brood Spider's web: a glob of cobweb that leaves a short-lived brood cobweb wherever it lands.
// Cast from a Tome of Weaving it spreads into a small cluster, and poisons whatever it hits directly
public class WebSpitEntity extends ThrowableItemProjectile {
    public static final float DAMAGE = 1.0F;
    // Player-cast direct hit: Poison I for this long (ticks)
    public static final int CAST_POISON_TICKS = 5 * 20;
    // Item model the glob is drawn with: a flat sprite of the brood cobweb
    public static final Identifier MODEL = MitchellEnhancedVanilla.id("brood_cobweb");

    public WebSpitEntity(EntityType<? extends WebSpitEntity> type, Level level) {
        super(type, level);
    }

    public WebSpitEntity(Level level, LivingEntity owner) {
        super(ModEntities.WEB_SPIT, owner, level, globStack());
    }

    // A cobweb item showing the brood cobweb texture instead of the vanilla one
    private static ItemStack globStack() {
        ItemStack stack = new ItemStack(Items.COBWEB);
        stack.set(DataComponents.ITEM_MODEL, MODEL);
        return stack;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBWEB;
    }

    // Set when a shield caught it; onHitEntity runs inside super.onHit, before the web is placed
    private boolean blocked;

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!(level() instanceof ServerLevel level)) return;
        boolean hurt = hit.getEntity().hurtServer(level, damageSources().thrown(this, getOwner()), DAMAGE);
        // A failed hit on someone holding up a shield was blocked (rather than, say, landing during i-frames)
        blocked = !hurt && hit.getEntity() instanceof LivingEntity living && living.getItemBlockingWith() != null;
        if (hurt && castByPlayer() && hit.getEntity() instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, CAST_POISON_TICKS, 0), getOwner());
        }
    }

    private boolean castByPlayer() {
        return getOwner() instanceof Player;
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof ServerLevel level) {
            if (!blocked) placeWebs(level, webPos(hit));
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.BROOD_COBWEB.defaultBlockState()), getX(), getY(), getZ(), 8, 0.1, 0.1, 0.1, 0.05);
            discard();
        }
    }

    // Around whatever it hit: the entity's feet, or the open side of the block face it struck
    private BlockPos webPos(HitResult hit) {
        if (hit instanceof EntityHitResult entityHit) return entityHit.getEntity().blockPosition();
        if (hit instanceof BlockHitResult blockHit) return blockHit.getBlockPos().relative(blockHit.getDirection());
        return blockPosition();
    }

    // One web for a spider's spit; a plus of five (centre and its four horizontal neighbours) for a player's cast
    private void placeWebs(ServerLevel level, BlockPos centre) {
        boolean placed = placeWeb(level, centre);
        if (castByPlayer()) {
            for (Direction side : Direction.Plane.HORIZONTAL) placed |= placeWeb(level, centre.relative(side));
        }
        if (placed) level.playSound(null, centre, SoundEvents.COBWEB_PLACE, SoundSource.HOSTILE, 1.0F, 0.8F);
    }

    // Same rule as the Weaving effect: only into air or other replaceable blocks (grass, snow layers), never into water or lava
    private static boolean placeWeb(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).canBeReplaced() || !level.getFluidState(pos).isEmpty()) return false;
        level.setBlockAndUpdate(pos, ModBlocks.BROOD_COBWEB.defaultBlockState());
        return true;
    }
}
