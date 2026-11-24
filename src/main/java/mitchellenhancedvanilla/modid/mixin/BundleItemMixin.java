package mitchellenhancedvanilla.modid.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mitchellenhancedvanilla.modid.item.QuiverItem;
import mitchellenhancedvanilla.modid.quiver.QuiverCapacity;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BundleItem.class)
public abstract class BundleItemMixin {
    // Quivers insert with their larger capacity
    @WrapOperation(method = {"overrideStackedOnOther", "overrideOtherStackedOnMe"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/BundleContents;asMutable()Lnet/minecraft/world/item/component/BundleContents$Mutable;"))
    private BundleContents.Mutable mev$quiverCapacity(BundleContents contents, Operation<BundleContents.Mutable> original) {
        BundleContents.Mutable mutable = original.call(contents);
        if ((Object) this instanceof QuiverItem) {
            ((QuiverCapacity) mutable).mev$setCapacity(QuiverItem.CAPACITY);
        }
        return mutable;
    }
}
