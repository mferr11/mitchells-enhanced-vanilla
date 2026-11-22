package mitchellenhancedvanilla.modid.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mitchellenhancedvanilla.modid.entity.EmeraldArrowEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    // Flags Emerald Arrow kills while loot drops, for the Looting bonus
    @WrapMethod(method = "dropAllDeathLoot")
    private void mev$emeraldArrowLoot(ServerLevel level, DamageSource source, Operation<Void> original) {
        boolean emeraldArrow = source.getDirectEntity() instanceof EmeraldArrowEntity;
        if (emeraldArrow) EmeraldArrowEntity.droppingLoot = true;
        try {
            original.call(level, source);
        } finally {
            if (emeraldArrow) EmeraldArrowEntity.droppingLoot = false;
        }
    }
}
