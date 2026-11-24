package mitchellenhancedvanilla.modid.client.hud;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.ModItems;
import mitchellenhancedvanilla.modid.client.render.FadedItems;
import mitchellenhancedvanilla.modid.quiver.Quiver;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

// Current and next quiver arrow, beyond the offhand slot
public final class QuiverHotbar {
    private static final Identifier HOTBAR_SPRITE = Identifier.withDefaultNamespace("hud/hotbar");
    private static final int HOTBAR_WIDTH = 182;
    private static final int HOTBAR_HEIGHT = 22;
    private static final int HOTBAR_HALF = 91;
    private static final int OFFHAND_WIDTH = 29; // vanilla offhand sprite, gap included
    private static final int SLOT_SIZE = 20;
    private static final int SLOTS = 2;
    private static final int STRIP_WIDTH = SLOTS * SLOT_SIZE + 2;
    private static final int GAP = 7;
    private static final float NEXT_ALPHA = 0.5F;
    private static final int POP_TICKS = 5; // vanilla pickup pop length
    private static final int MS_PER_TICK = 50;

    // Arrows shown last frame, to spot changes
    private static final ItemStack[] lastShown = {ItemStack.EMPTY, ItemStack.EMPTY};
    private static boolean initialised = false; // no pop for the first frame after joining
    private static long popStartMs = Long.MIN_VALUE / 2;

    private QuiverHotbar() {}

    public static void register() {
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.HOTBAR,
            MitchellEnhancedVanilla.id("quiver_hotbar"),
            QuiverHotbar::extract);
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.getCameraEntity() instanceof Player player) || player.isSpectator()) return;
        if (!Quiver.get(player).is(ModItems.QUIVER)) return;

        // Space for the offhand slot is kept even when it is empty
        boolean onLeft = player.getMainArm() == HumanoidArm.RIGHT;
        int center = graphics.guiWidth() / 2;
        int offset = HOTBAR_HALF + OFFHAND_WIDTH + GAP;
        int left = onLeft ? center - offset - STRIP_WIDTH : center + offset;
        int top = graphics.guiHeight() - HOTBAR_HEIGHT;

        ItemStack[] shown = new ItemStack[SLOTS];
        for (int i = 0; i < SLOTS; i++) shown[i] = Quiver.peek(player, i);
        float pop = popTime(shown);

        // Current arrow sits nearest the hotbar; the next one is faded
        for (int i = 0; i < SLOTS; i++) {
            float alpha = i == 0 ? 1.0F : NEXT_ALPHA;
            int slot = slotFor(i, onLeft);
            drawFrame(graphics, left, top, slot, alpha);

            ItemStack stack = shown[i];
            if (stack.isEmpty()) continue;
            int x = left + 3 + slot * SLOT_SIZE;
            int y = top + 3;
            int seed = 200 + i; // past the hotbar's

            Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            applyPop(pose, pop, x, y);
            if (alpha < 1.0F) {
                FadedItems.item(graphics, player, stack, x, y, seed, alpha);
            } else {
                graphics.item(player, stack, x, y, seed);
            }
            pose.popMatrix();

            if (alpha < 1.0F) {
                FadedItems.count(graphics, mc.font, stack, x, y, alpha);
            } else {
                graphics.itemDecorations(mc.font, stack, x, y);
            }
        }
    }

    // Restarts the pop when the shown arrows change (rotating, adding a new type, running out)
    private static float popTime(ItemStack[] shown) {
        boolean changed = false;
        for (int i = 0; i < SLOTS; i++) {
            if (!ItemStack.isSameItemSameComponents(shown[i], lastShown[i])) changed = true;
        }
        if (changed) {
            if (initialised) popStartMs = Util.getMillis();
            for (int i = 0; i < SLOTS; i++) lastShown[i] = shown[i].copy();
            initialised = true;
        }
        // Real time, so respawns and dimension changes (tick count resets) can't break it
        return POP_TICKS - (Util.getMillis() - popStartMs) / (float) MS_PER_TICK;
    }

    // Vanilla's hotbar pickup squash (Hud.extractSlot)
    private static void applyPop(Matrix3x2fStack pose, float pop, int x, int y) {
        if (pop <= 0) return;
        float scale = 1.0F + pop / POP_TICKS;
        pose.translate(x + 8, y + 12);
        pose.scale(1.0F / scale, (scale + 1.0F) / 2.0F);
        pose.translate(-(x + 8), -(y + 12));
    }

    // One slot's share of the vanilla hotbar sprite; the last slot also takes its right border
    private static void drawFrame(GuiGraphicsExtractor graphics, int left, int top, int slot, float alpha) {
        int color = ARGB.white(alpha);
        int u = slot == 0 ? 0 : 1 + slot * SLOT_SIZE;
        int width = slot == 0 ? SLOT_SIZE + 1 : SLOT_SIZE;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, HOTBAR_WIDTH, HOTBAR_HEIGHT,
            u, 0, left + u, top, width, HOTBAR_HEIGHT, color);
        if (slot == SLOTS - 1) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, HOTBAR_WIDTH, HOTBAR_HEIGHT,
                HOTBAR_WIDTH - 1, 0, left + STRIP_WIDTH - 1, top, 1, HOTBAR_HEIGHT, color);
        }
    }

    // Strip slot for a quiver position, counting outward from the hotbar
    private static int slotFor(int index, boolean onLeft) {
        return onLeft ? SLOTS - 1 - index : index;
    }
}
