package mitchellenhancedvanilla.modid.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mitchellenhancedvanilla.modid.entity.GoldArrowEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    // Flags Gold Arrow kills while loot drops, for the Looting bonus
    @WrapMethod(method = "dropAllDeathLoot")
    private void mev$goldArrowLoot(ServerLevel level, DamageSource source, Operation<Void> original) {
        boolean goldArrow = source.getDirectEntity() instanceof GoldArrowEntity;
        if (goldArrow) GoldArrowEntity.droppingLoot = true;
        try {
            original.call(level, source);
        } finally {
            if (goldArrow) GoldArrowEntity.droppingLoot = false;
        }
    }
}
