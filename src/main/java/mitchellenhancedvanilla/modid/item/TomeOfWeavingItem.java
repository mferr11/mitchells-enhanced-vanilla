package mitchellenhancedvanilla.modid.item;

import mitchellenhancedvanilla.modid.entity.WebSpitEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Spits a Brood Spider's web glob where the player is looking; each cast costs durability
public class TomeOfWeavingItem extends Item {
    public static final int DURABILITY = 128;
    // Same speed as the spider's spit, but aimed true
    public static final float CAST_SPEED = 1.3F;
    public static final float CAST_INACCURACY = 0.0F;
    public static final int COOLDOWN_TICKS = 15;

    public TomeOfWeavingItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LLAMA_SPIT, SoundSource.PLAYERS, 1.0F,
            0.6F + level.getRandom().nextFloat() * 0.2F);
        if (level instanceof ServerLevel server) {
            WebSpitEntity spit = new WebSpitEntity(server, player);
            spit.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, CAST_SPEED, CAST_INACCURACY);
            server.addFreshEntity(spit);
            stack.hurtAndBreak(1, player, hand);
        }
        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        return InteractionResult.SUCCESS;
    }
}
