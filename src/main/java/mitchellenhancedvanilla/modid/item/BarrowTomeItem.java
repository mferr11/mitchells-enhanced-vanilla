package mitchellenhancedvanilla.modid.item;

import mitchellenhancedvanilla.modid.entity.SoulFlameEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Hurls the Barrow King's soul flame where the player is looking; each cast costs durability
public class BarrowTomeItem extends Item {
    public static final int DURABILITY = 128;
    // Same speed as the King's throw, but aimed true
    public static final float CAST_SPEED = 1.6F;
    public static final float CAST_INACCURACY = 0.0F;
    // Stand-in limit until mana exists (ticks)
    public static final int COOLDOWN_TICKS = 20;

    public BarrowTomeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 0.6F);
        if (level instanceof ServerLevel server) {
            SoulFlameEntity flame = new SoulFlameEntity(server, player);
            flame.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, CAST_SPEED, CAST_INACCURACY);
            server.addFreshEntity(flame);
            stack.hurtAndBreak(1, player, hand);
        }
        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        return InteractionResult.SUCCESS;
    }
}
