package mitchellenhancedvanilla.modid.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import mitchellenhancedvanilla.modid.client.render.FadedItemState;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    // Tint faded items as they are copied out of the item atlas
    @WrapOperation(method = "submitBlitFromItemAtlas",
        at = @At(value = "NEW", target = "net/minecraft/client/renderer/state/gui/BlitRenderState"))
    private BlitRenderState mev$fadeItem(RenderPipeline pipeline, TextureSetup textures, Matrix3x2fc pose,
                                         int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1,
                                         int color, ScreenRectangle scissor, ScreenRectangle bounds,
                                         Operation<BlitRenderState> original, @Local(argsOnly = true) GuiItemRenderState item) {
        int alpha = ((FadedItemState) (Object) item).mev$alpha();
        if (alpha < 255) {
            color = ARGB.color(alpha, alpha, alpha, alpha); // the atlas is premultiplied, so fade rgb too
        }
        return original.call(pipeline, textures, pose, x0, y0, x1, y1, u0, u1, v0, v1, color, scissor, bounds);
    }
}
