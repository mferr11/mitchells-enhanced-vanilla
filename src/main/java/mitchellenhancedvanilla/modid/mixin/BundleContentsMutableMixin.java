package mitchellenhancedvanilla.modid.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mitchellenhancedvanilla.modid.quiver.QuiverCapacity;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

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
}
