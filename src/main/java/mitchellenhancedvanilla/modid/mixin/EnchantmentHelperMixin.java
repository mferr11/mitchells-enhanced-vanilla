package mitchellenhancedvanilla.modid.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mitchellenhancedvanilla.modid.entity.EmeraldArrowEntity;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    // Emerald Arrow kills count as Looting, for every loot table that checks the killer's Looting
    @ModifyReturnValue(method = "getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I", at = @At("RETURN"))
    private static int mev$goldArrowLooting(int level, Holder<Enchantment> enchantment, LivingEntity entity) {
        if (EmeraldArrowEntity.droppingLoot && enchantment.is(Enchantments.LOOTING)) {
            return Math.max(level, EmeraldArrowEntity.LOOTING_LEVEL);
        }
        return level;
    }
}
