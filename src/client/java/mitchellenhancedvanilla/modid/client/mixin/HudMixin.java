package mitchellenhancedvanilla.modid.client.mixin;

import mitchellenhancedvanilla.modid.client.hud.ArmourHotbar;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Hud.class)
public abstract class HudMixin {
    // Move the hotbar attack indicator past the armour slots
    @ModifyArg(method = "extractItemHotbar",
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;attackIndicator()Lnet/minecraft/client/OptionInstance;")),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"),
        index = 2)
    private int mev$shiftIndicatorBackground(int x) {
        return x + ArmourHotbar.indicatorShift();
    }

    @ModifyArg(method = "extractItemHotbar",
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;attackIndicator()Lnet/minecraft/client/OptionInstance;")),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIIIII)V"),
        index = 6)
    private int mev$shiftIndicatorProgress(int x) {
        return x + ArmourHotbar.indicatorShift();
    }
}
