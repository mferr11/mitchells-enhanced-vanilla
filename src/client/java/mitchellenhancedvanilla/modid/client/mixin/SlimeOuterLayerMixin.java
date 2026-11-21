package mitchellenhancedvanilla.modid.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import mitchellenhancedvanilla.modid.client.render.FrozenTint;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Frozen tint for the slime's translucent outer gel
@Mixin(SlimeOuterLayer.class)
public abstract class SlimeOuterLayerMixin {
    // The short submitModel overload has no tint (its last int is the outline colour), so use the full one
    @WrapOperation(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/SlimeRenderState;FF)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;III)V"))
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mev$frozenOuterTint(
        OrderedSubmitNodeCollector collector, Model<?> model, Object renderState, PoseStack poseStack,
        RenderType renderType, int light, int overlay, int outlineColor, Operation<Void> original) {
        if (renderState instanceof LivingEntityRenderState state && state.isFullyFrozen) {
            collector.submitModel((Model) model, renderState, poseStack, renderType, light, overlay,
                FrozenTint.apply(-1, state), null, outlineColor);
            return;
        }
        original.call(collector, model, renderState, poseStack, renderType, light, overlay, outlineColor);
    }
}
