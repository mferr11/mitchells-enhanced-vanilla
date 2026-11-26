package mitchellenhancedvanilla.modid.mixin;

import net.minecraft.world.entity.projectile.EyeOfEnder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EyeOfEnder.class)
public interface EyeOfEnderAccessor {
    // Whether it drops back as an item when it finishes flying, rather than shattering
    @Accessor("surviveAfterDeath")
    void mev$setSurviveAfterDeath(boolean survive);
}
