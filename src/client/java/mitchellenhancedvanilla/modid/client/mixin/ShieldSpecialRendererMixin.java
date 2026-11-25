package mitchellenhancedvanilla.modid.client.mixin;

import java.util.Map;

import com.llamalad7.mixinextras.sugar.Local;
import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.entity.BarrowKingEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// Vanilla always draws the plain shield texture; the Barrow King's shields get their own
@Mixin(ShieldSpecialRenderer.class)
public abstract class ShieldSpecialRendererMixin {
    // Item model id -> texture in textures/entity/shield/ (already stitched into the shield atlas for every namespace)
    @Unique
    private static final Map<Identifier, SpriteId> MEV_SHIELD_TEXTURES = Map.of(
        // The item players get
        MitchellEnhancedVanilla.id("kings_shield"), Sheets.SHIELD_MAPPER.apply(MitchellEnhancedVanilla.id("barrow_king_shield_player")),
        // The one the King carries
        BarrowKingEntity.SHIELD_MODEL, Sheets.SHIELD_MAPPER.apply(MitchellEnhancedVanilla.id("barrow_king_shield")));

    @ModifyVariable(method = "submit", at = @At("STORE"))
    private SpriteId mev$barrowKingShield(SpriteId base, @Local(argsOnly = true) @Nullable DataComponentMap components) {
        if (components == null) return base;
        Identifier model = components.get(DataComponents.ITEM_MODEL);
        return model != null ? MEV_SHIELD_TEXTURES.getOrDefault(model, base) : base;
    }
}
