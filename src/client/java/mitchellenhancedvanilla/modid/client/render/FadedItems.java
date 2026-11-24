package mitchellenhancedvanilla.modid.client.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

// Draws GUI items with transparency (see GuiItemRenderStateMixin / GuiRendererMixin)
public final class FadedItems {
    // Picked up by the next GuiItemRenderState created
    private static int pendingAlpha = 255;

    private FadedItems() {}

    public static int pendingAlpha() {
        return pendingAlpha;
    }

    public static void item(GuiGraphicsExtractor graphics, LivingEntity entity, ItemStack stack, int x, int y, int seed, float alpha) {
        pendingAlpha = Math.round(alpha * 255);
        try {
            graphics.item(entity, stack, x, y, seed);
        } finally {
            pendingAlpha = 255;
        }
    }

    // Stack count in vanilla's position, faded
    public static void count(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y, float alpha) {
        if (stack.getCount() == 1) return;
        String text = String.valueOf(stack.getCount());
        graphics.text(font, text, x + 19 - 2 - font.width(text), y + 6 + 3, ARGB.white(alpha), true);
    }
}
