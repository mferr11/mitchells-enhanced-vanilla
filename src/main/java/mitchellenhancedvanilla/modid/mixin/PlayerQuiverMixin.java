package mitchellenhancedvanilla.modid.mixin;

import mitchellenhancedvanilla.modid.quiver.Quiver;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerQuiverMixin {
    // Quiver ammo comes after hand-held arrows but before the inventory
    @Inject(method = "getProjectile",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getAllSupportedProjectiles()Ljava/util/function/Predicate;"),
        cancellable = true)
    private void mev$quiverAmmo(ItemStack weapon, CallbackInfoReturnable<ItemStack> cir) {
        ProjectileWeaponItem item = (ProjectileWeaponItem) weapon.getItem();
        ItemStack ammo = Quiver.takeAmmo((Player) (Object) this, item.getAllSupportedProjectiles());
        if (!ammo.isEmpty()) cir.setReturnValue(ammo);
    }
}
