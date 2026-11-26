package mitchellenhancedvanilla.modid.item;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.mixin.EyeOfEnderAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;

// Thrown like an Eye of Ender, it flies towards the nearest brood nest, then always drops back down to be picked up again
public class WeaverEyeItem extends Item {
    public static final TagKey<Structure> LOCATED = TagKey.create(Registries.STRUCTURE, MitchellEnhancedVanilla.id("brood_nests"));
    // How far it searches (chunks), as the Eye of Ender does
    public static final int SEARCH_RADIUS = 100;

    public WeaverEyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(hand);
        BlockPos nest = server.findNearestMapStructure(LOCATED, player.blockPosition(), SEARCH_RADIUS, false);
        if (nest == null) {
            player.sendOverlayMessage(Component.translatable(getDescriptionId() + ".none").withStyle(ChatFormatting.GRAY));
            return InteractionResult.FAIL;
        }

        // Vanilla's flying eye, carrying this item; it flies the way the Eye of Ender does, but never shatters
        EyeOfEnder eye = new EyeOfEnder(level, player.getX(), player.getY(0.5), player.getZ());
        eye.setItem(stack);
        eye.signalTo(Vec3.atLowerCornerOf(nest));
        ((EyeOfEnderAccessor) eye).mev$setSurviveAfterDeath(true);
        level.gameEvent(GameEvent.PROJECTILE_SHOOT, eye.position(), GameEvent.Context.of(player));
        level.addFreshEntity(eye);

        float pitch = Mth.lerp(level.getRandom().nextFloat(), 0.33F, 0.5F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 1.0F, pitch);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPIDER_AMBIENT, SoundSource.NEUTRAL, 0.6F, 1.6F);
        stack.consume(1, player);
        return InteractionResult.SUCCESS_SERVER;
    }
}
