package mitchellenhancedvanilla.modid.entity;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import mitchellenhancedvanilla.modid.ModBlocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

// Brood Spider's web: a glob of cobweb that leaves a short-lived brood cobweb wherever it lands
public class WebSpitEntity extends ThrowableItemProjectile {
    public static final float DAMAGE = 1.0F;
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

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!(level() instanceof ServerLevel level)) return;
        hit.getEntity().hurtServer(level, damageSources().thrown(this, getOwner()), DAMAGE);
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof ServerLevel level) {
            placeWeb(level, webPos(hit));
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

    // Same rule as the Weaving effect: only into air or other replaceable blocks (grass, snow layers)
    private static void placeWeb(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).canBeReplaced()) return;
        level.setBlockAndUpdate(pos, ModBlocks.BROOD_COBWEB.defaultBlockState());
        level.playSound(null, pos, SoundEvents.COBWEB_PLACE, SoundSource.HOSTILE, 1.0F, 0.8F);
    }
}
