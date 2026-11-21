package mitchellenhancedvanilla.modid.mixin;

import mitchellenhancedvanilla.modid.BowStats;
import net.minecraft.world.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(BowItem.class)
public class BowItemMixin {
    @ModifyArg(
        method = "releaseUsing",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/BowItem;shoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/world/entity/LivingEntity;)V"),
        index = 5)
    private float mev$velocity(float velocity) {
        return velocity * BowStats.VELOCITY_MULTIPLIER;
    }

    @ModifyConstant(method = "getPowerForTime", constant = @Constant(floatValue = 20.0F))
    private static float mev$drawTime(float original) {
        return BowStats.DRAW_TICKS; // vanilla is 20 ticks
    }
}
