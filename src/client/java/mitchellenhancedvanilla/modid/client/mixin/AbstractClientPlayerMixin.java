package mitchellenhancedvanilla.modid.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mitchellenhancedvanilla.modid.BowStats;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    // Bow zoom-in reaches its max at full draw. Vanilla divides by 20
    @ModifyConstant(method = "getFieldOfViewModifier", constant = @Constant(floatValue = 20.0F))
    private float mev$bowZoomDrawTime(float original) {
        return BowStats.of(((AbstractClientPlayer) (Object) this).getUseItem()).drawTicks();
    }

    // Vanilla only zooms for the wooden bow
    @WrapOperation(method = "getFieldOfViewModifier",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"))
    private boolean mev$anyBowZooms(ItemStack stack, Object item, Operation<Boolean> original) {
        return original.call(stack, item) || (item == Items.BOW && stack.getItem() instanceof BowItem);
    }
}
