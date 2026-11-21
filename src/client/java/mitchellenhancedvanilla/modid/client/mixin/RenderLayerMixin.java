package mitchellenhancedvanilla.modid.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import mitchellenhancedvanilla.modid.client.render.FrozenTint;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.UvMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Frozen tint for layers like villager clothing and sheep wool
@Mixin(RenderLayer.class)
public abstract class RenderLayerMixin {
    @WrapOperation(
        method = "renderColoredCutoutModel",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V"))
    private static void mev$frozenLayerTint(
        OrderedSubmitNodeCollector collector, Model<?> model, Object renderState, PoseStack poseStack,
        RenderType renderType, int light, int overlay, int color, UvMapping uvMapping, int outlineColor,
        Operation<Void> original) {
        if (renderState instanceof LivingEntityRenderState state) {
            color = FrozenTint.apply(color, state);
        }
        original.call(collector, model, renderState, poseStack, renderType, light, overlay, color, uvMapping, outlineColor);
    }
}
