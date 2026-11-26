package mitchellenhancedvanilla.modid.mixin;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SimpleMutableContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleMutableContainer.class)
public interface SimpleMutableContainerAccessor {
    @Accessor("items")
    List<ItemStack> mev$items();
}
