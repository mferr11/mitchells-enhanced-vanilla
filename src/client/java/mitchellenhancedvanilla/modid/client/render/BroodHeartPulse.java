package mitchellenhancedvanilla.modid.client.render;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

// The Brood Heart side texture's animation, tracked so the heartbeat sound can follow it
public final class BroodHeartPulse {
    public static final Identifier SPRITE = MitchellEnhancedVanilla.id("block/brood_heart");
    public static SpriteContents.@Nullable AnimationState animation;

    private BroodHeartPulse() {}
}
