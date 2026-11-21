package mitchellenhancedvanilla.modid.client.render;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;

// Shared renderer for the mod's arrows
public class ModArrowRenderer<T extends AbstractArrow> extends ArrowRenderer<T, ArrowRenderState> {
    private final Identifier texture;

    public ModArrowRenderer(EntityRendererProvider.Context context, Identifier texture) {
        super(context);
        this.texture = texture;
    }

    // Texture: <namespace>:textures/entity/projectiles/<entity id>.png
    public static <T extends AbstractArrow> void register(EntityType<T> type) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        Identifier texture = id.withPath(path -> "textures/entity/projectiles/" + path + ".png");
        EntityRenderers.register(type, context -> new ModArrowRenderer<T>(context, texture));
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState state) {
        return texture;
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}
