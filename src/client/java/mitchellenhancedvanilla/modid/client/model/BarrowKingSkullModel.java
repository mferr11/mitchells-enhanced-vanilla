package mitchellenhancedvanilla.modid.client.model;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.object.skull.SkullModel;

// Skull with the Tarnished Crown worn over it, as an inflated outer cube like a helmet
public final class BarrowKingSkullModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(MitchellEnhancedVanilla.id("barrow_king_skull"), "main");
    // Vanilla helmets inflate the head cube by 1 pixel
    private static final CubeDeformation HELMET_DEFORMATION = new CubeDeformation(1.0F);

    private BarrowKingSkullModel() {}

    public static void register() {
        ModelLayerRegistry.registerModelLayer(LAYER, BarrowKingSkullModel::createLayer);
    }

    // Texture (scripts/generate_barrow_king_skull.py): head in rows 0-31, crown helmet layer in rows 32-63
    private static LayerDefinition createLayer() {
        MeshDefinition mesh = SkullModel.createHeadModel();
        mesh.getRoot().getChild("head").addOrReplaceChild("crown",
            CubeListBuilder.create().texOffs(0, 32).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, HELMET_DEFORMATION),
            PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }
}
