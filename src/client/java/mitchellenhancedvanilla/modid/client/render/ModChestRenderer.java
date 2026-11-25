package mitchellenhancedvanilla.modid.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import mitchellenhancedvanilla.modid.block.ModChestBlock;
import mitchellenhancedvanilla.modid.block.entity.ModChestBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

// Vanilla single-chest model and lid animation, textured per block (Barrow, Brood) (picked up by the vanilla chest atlas)
public class ModChestRenderer extends ChestRenderer<ModChestBlockEntity> {
    private final SpriteGetter sprites;
    private final ChestModel model;

    public ModChestRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.sprites = context.sprites();
        this.model = new ChestModel(context.bakeLayer(ModelLayers.CHEST));
    }

    public static class State extends ChestRenderState {
        public @Nullable SpriteId sprite;
    }

    @Override
    public ChestRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ModChestBlockEntity blockEntity, ChestRenderState state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        if (state instanceof State locked && blockEntity.getBlockState().getBlock() instanceof ModChestBlock block) {
            locked.sprite = Sheets.CHEST_MAPPER.apply(block.texture());
        }
    }

    // Same as vanilla's submit, minus the material lookup
    @Override
    public void submit(ChestRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!(state instanceof State locked) || locked.sprite == null) return;
        SpriteId sprite = locked.sprite;
        poseStack.pushPose();
        poseStack.mulPose(modelTransformation(state.facing));
        float open = 1.0F - state.open;
        open = 1.0F - open * open * open;
        submitNodeCollector.submitModel(model, open, poseStack, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, sprite, sprites, 0);
        if (state.breakProgress != null) {
            submitNodeCollector.order(1).submitCrumblingOverlay(model, open, poseStack, sprite.renderType(model.renderType()),
                state.lightCoords, OverlayTexture.NO_OVERLAY, -1, state.breakProgress);
        }
        poseStack.popPose();
    }
}
