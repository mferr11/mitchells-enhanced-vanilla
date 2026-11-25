package mitchellenhancedvanilla.modid.client.mixin;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.ModBlocks;
import net.minecraft.client.model.geom.EntityModelSet;
import mitchellenhancedvanilla.modid.client.model.BarrowKingSkullModel;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SkullBlock;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Vanilla only knows its own skull types; this adds the Barrow King's (placed, worn and as an item)
@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin {
    // Head plus the crown layer, built by scripts/generate_barrow_king_skull.py
    @Unique
    private static final Identifier BARROW_KING_TEXTURE = MitchellEnhancedVanilla.id("textures/entity/barrow_king/barrow_king_skull.png");

    @Inject(method = "createModel", at = @At("HEAD"), cancellable = true)
    private static void mev$barrowKingModel(EntityModelSet modelSet, SkullBlock.Type type, CallbackInfoReturnable<SkullModelBase> cir) {
        if (type == ModBlocks.BARROW_KING_SKULL_TYPE) {
            cir.setReturnValue(new SkullModel(modelSet.bakeLayer(BarrowKingSkullModel.LAYER)));
        }
    }

    @Inject(method = "getSkullRenderType", at = @At("HEAD"), cancellable = true)
    private static void mev$barrowKingTexture(SkullBlock.Type type, @Nullable Identifier texture, CallbackInfoReturnable<RenderType> cir) {
        if (texture == null && type == ModBlocks.BARROW_KING_SKULL_TYPE) {
            cir.setReturnValue(RenderTypes.entityCutoutZOffset(BARROW_KING_TEXTURE));
        }
    }
}
