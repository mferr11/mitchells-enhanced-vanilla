package mitchellenhancedvanilla.modid.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.DataResult;
import mitchellenhancedvanilla.modid.quiver.QuiverCapacity;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientBundleTooltip.class)
public abstract class ClientBundleTooltipMixin implements QuiverCapacity {
    @Unique
    private int mev$capacity = 1;

    @Override
    public void mev$setCapacity(int stacks) {
        mev$capacity = stacks;
    }

    // Fill bar and "full" text measured against the quiver's capacity
    @ModifyExpressionValue(method = "extractImage",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/BundleContents;weight()Lcom/mojang/serialization/DataResult;"))
    private DataResult<Fraction> mev$scaleWeight(DataResult<Fraction> weight) {
        if (mev$capacity == 1) return weight;
        Fraction capacity = Fraction.getFraction(mev$capacity, 1);
        return weight.map(w -> w.divideBy(capacity));
    }
}
