package mitchellenhancedvanilla.modid.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.ArrayList;
import java.util.List;
import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.SmithingTemplateItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SmithingTemplateItem.class)
public class SmithingTemplateItemMixin {
    // Bow outline joins the netherite template's cycling base-slot hints
    @ModifyReturnValue(method = "createNetheriteUpgradeIconList", at = @At("RETURN"))
    private static List<Identifier> mev$addBowIcon(List<Identifier> icons) {
        List<Identifier> withBow = new ArrayList<>(icons);
        withBow.add(MitchellEnhancedVanilla.id("container/slot/bow"));
        return List.copyOf(withBow);
    }
}
