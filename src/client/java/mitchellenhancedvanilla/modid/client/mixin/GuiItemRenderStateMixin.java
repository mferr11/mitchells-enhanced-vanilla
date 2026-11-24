package mitchellenhancedvanilla.modid.client.mixin;

import mitchellenhancedvanilla.modid.client.render.FadedItemState;
import mitchellenhancedvanilla.modid.client.render.FadedItems;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiItemRenderState.class)
public abstract class GuiItemRenderStateMixin implements FadedItemState {
    @Unique
    private int mev$alpha = 255;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mev$captureAlpha(CallbackInfo ci) {
        mev$alpha = FadedItems.pendingAlpha();
    }

    @Override
    public int mev$alpha() {
        return mev$alpha;
    }
}
