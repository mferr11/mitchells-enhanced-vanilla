package mitchellenhancedvanilla.modid.mixin;

import mitchellenhancedvanilla.modid.ModItems;
import mitchellenhancedvanilla.modid.item.KingsShieldItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Runs whenever a melee hit lands on a raised shield (players call super, so this covers them too)
@Mixin(LivingEntity.class)
public abstract class LivingEntityShieldBashMixin {
    @Inject(method = "blockUsingItem", at = @At("HEAD"))
    private void mev$barrowShieldBash(ServerLevel level, LivingEntity attacker, DamageSource source, float damage,
                                      boolean fullyBlocked, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        ItemStack blocking = self.getItemBlockingWith();
        if (blocking != null && blocking.is(ModItems.KINGS_SHIELD)) {
            KingsShieldItem.bash(level, self, attacker, source);
        }
    }
}
