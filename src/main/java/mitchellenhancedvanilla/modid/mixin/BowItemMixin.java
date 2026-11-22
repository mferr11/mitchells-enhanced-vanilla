package mitchellenhancedvanilla.modid.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mitchellenhancedvanilla.modid.BowStats;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(BowItem.class)
public class BowItemMixin {
    // Shot power from this bow's own draw time
    @WrapOperation(method = "releaseUsing",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F"))
    private float mev$power(int ticks, Operation<Float> original, @Local(argsOnly = true) ItemStack bow) {
        return BowStats.of(bow).power(ticks);
    }

    @ModifyArg(
        method = "releaseUsing",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/BowItem;shoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/world/entity/LivingEntity;)V"),
        index = 5)
    private float mev$velocity(float velocity, @Local(argsOnly = true) ItemStack bow) {
        return velocity * BowStats.of(bow).velocityMultiplier();
    }

    @ModifyArg(
        method = "releaseUsing",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/BowItem;shoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/world/entity/LivingEntity;)V"),
        index = 6)
    private float mev$inaccuracy(float inaccuracy, @Local(argsOnly = true) ItemStack bow) {
        return BowStats.of(bow).inaccuracy();
    }

    // Other callers (skeleton AI) have no bow stack, so they get the wooden draw time
    @ModifyConstant(method = "getPowerForTime", constant = @Constant(floatValue = 20.0F))
    private static float mev$drawTime(float original) {
        return BowStats.WOODEN.drawTicks();
    }
}
