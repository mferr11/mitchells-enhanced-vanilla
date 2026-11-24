package mitchellenhancedvanilla.modid.mixin;

import java.util.List;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mitchellenhancedvanilla.modid.quiver.Quiver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ProjectileWeaponItem.class)
public abstract class ProjectileWeaponItemMixin {
    // Ammo drawn from a quiver copy is taken out of the quiver itself
    @WrapMethod(method = "draw")
    private static List<ItemStack> mev$drawFromQuiver(ItemStack weapon, ItemStack ammo, LivingEntity shooter, Operation<List<ItemStack>> original) {
        boolean fromQuiver = Quiver.isQuiverAmmo(ammo);
        ItemStack type = ammo.copy(); // ammo may be emptied below
        int before = ammo.getCount();
        List<ItemStack> drawn = original.call(weapon, ammo, shooter);
        int used = before - ammo.getCount();
        if (fromQuiver && used > 0 && shooter instanceof Player player && !player.level().isClientSide()) {
            Quiver.consume(player, type, used);
        }
        return drawn;
    }
}
