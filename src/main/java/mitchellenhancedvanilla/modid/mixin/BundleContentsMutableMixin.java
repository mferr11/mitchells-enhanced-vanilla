package mitchellenhancedvanilla.modid.mixin;

import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mitchellenhancedvanilla.modid.quiver.QuiverCapacity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleContents.Mutable.class)
public abstract class BundleContentsMutableMixin implements QuiverCapacity {
    @Unique
    private int mev$capacity = 1;

    @Override
    public void mev$setCapacity(int stacks) {
        mev$capacity = stacks;
    }

    // Vanilla caps every bundle at a weight of one stack
    @ModifyExpressionValue(method = {"getMaxAmountToAdd", "canInsertNewSlots"},
        at = @At(value = "FIELD", target = "Lorg/apache/commons/lang3/math/Fraction;ONE:Lorg/apache/commons/lang3/math/Fraction;"))
    private Fraction mev$capacity(Fraction one) {
        return mev$capacity == 1 ? one : Fraction.getFraction(mev$capacity, 1);
    }

    // Vanilla merges same-item stacks without a size cap; past one stack of weight (quivers) that makes 128-arrow
    // stacks, which fail to sync and disconnect the player. Split them back into full stacks before saving.
    @Inject(method = "toImmutable", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/component/BundleContents$Mutable;mergeIdenticalStacks()V", shift = At.Shift.AFTER))
    private void mev$splitOversizedStacks(CallbackInfoReturnable<BundleContents> cir) {
        List<ItemStack> items = ((SimpleMutableContainerAccessor) this).mev$items();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            int max = stack.getMaxStackSize();
            if (stack.getCount() > max) {
                items.add(i + 1, stack.copyWithCount(stack.getCount() - max));
                items.set(i, stack.copyWithCount(max));
            }
        }
    }
}
