package mitchellenhancedvanilla.modid.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FirstPersonHandsAndItems.class)
public abstract class FirstPersonHandsAndItemsMixin {
    // Treat modded bows like the wooden bow when picking which hands to draw
    @WrapOperation(method = "evaluateWhichHandsToRender",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"))
    private static boolean mev$anyBow(ItemStack stack, Object item, Operation<Boolean> original) {
        return original.call(stack, item) || (item == Items.BOW && stack.getItem() instanceof BowItem);
    }
}
