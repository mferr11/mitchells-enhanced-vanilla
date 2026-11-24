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
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

// Previous, current and next quiver arrow, beyond the offhand slot
public final class QuiverHotbar {
    private static final Identifier HOTBAR_SPRITE = Identifier.withDefaultNamespace("hud/hotbar");
    private static final int HOTBAR_WIDTH = 182;
    private static final int HOTBAR_HEIGHT = 22;
    private static final int HOTBAR_HALF = 91;
    private static final int OFFHAND_WIDTH = 29; // vanilla offhand sprite, gap included
    private static final int BOX = 22; // one slot with its border
    private static final int GAP = 7;
    private static final float SIDE_SCALE = 0.8F;
    private static final float SIDE_ALPHA = 0.5F;
    private static final float SIDE_BOX = BOX * SIDE_SCALE;
    private static final float STRIP_WIDTH = BOX + 2 * SIDE_BOX;
    private static final long SLIDE_MS = 150;

    // Strip order: previous, current, next
    private static final int SLOTS = 3;
    private static final int CURRENT = 1;

    // Per strip position, from just off the left (-1) to just off the right (3)
    private static final float[] POSITION_SCALE = {SIDE_SCALE, SIDE_SCALE, 1.0F, SIDE_SCALE, SIDE_SCALE};
    private static final float[] POSITION_ALPHA = {0.0F, SIDE_ALPHA, 1.0F, SIDE_ALPHA, 0.0F};
    private static final float[] POSITION_CENTER = {
        -SIDE_BOX / 2, SIDE_BOX / 2, SIDE_BOX + BOX / 2F, SIDE_BOX + BOX + SIDE_BOX / 2, SIDE_BOX + BOX + SIDE_BOX * 1.5F};

    // Arrows shown last frame, to spot rotations
    private static ItemStack[] lastShown = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    private static boolean initialised = false; // no slide for the first frame after joining
    private static long slideStartMs = Long.MIN_VALUE / 2;
    // Arrows leaving on the left during a slide: the old previous and the old current
    private static ItemStack leavingPrevious = ItemStack.EMPTY;
    private static ItemStack leavingCurrent = ItemStack.EMPTY;

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
        float left = onLeft ? center - offset - STRIP_WIDTH : center + offset;
        int top = graphics.guiHeight() - HOTBAR_HEIGHT;

        for (int i = 0; i < SLOTS; i++) drawFrame(graphics, i, left, top);

        ItemStack[] shown = shownArrows(player);
        float slide = slideOffset(shown);
        // Arrows sliding in or out are cut off at the strip's outer borders
        graphics.enableScissor(Mth.floor(left) + 1, top, Mth.ceil(left + STRIP_WIDTH) - 1, top + BOX);
        for (int i = 0; i < SLOTS; i++) {
            drawArrow(graphics, mc, player, shown[i], i + slide, 1.0F, left, top, 200 + i); // seeds past the hotbar's
        }
        if (slide > 0) {
            drawArrow(graphics, mc, player, leavingPrevious, slide - 1, 1.0F, left, top, 203);
            // With no previous arrow to take its place, the old current fades as it slides left
            if (shown[0].isEmpty()) drawArrow(graphics, mc, player, leavingCurrent, slide, slide, left, top, 204);
        }
        graphics.disableScissor();
    }

    // Previous wraps round to the last stack; with only two stacks it would just repeat the next one
    private static ItemStack[] shownArrows(Player player) {
        int size = Quiver.contents(player).size();
        ItemStack previous = size > 2 ? Quiver.peek(player, size - 1) : ItemStack.EMPTY;
        return new ItemStack[] {previous, Quiver.peek(player, 0), Quiver.peek(player, 1)};
    }

    // 1 right after a rotation, easing to 0; other changes (new arrows, running out) just snap
    private static float slideOffset(ItemStack[] shown) {
        boolean changed = false;
        for (int i = 0; i < SLOTS; i++) {
            if (!ItemStack.isSameItemSameComponents(shown[i], lastShown[i])) changed = true;
        }
        if (changed) {
            boolean rotated = !shown[CURRENT].isEmpty()
                && ItemStack.isSameItemSameComponents(shown[CURRENT], lastShown[CURRENT + 1])
                && !ItemStack.isSameItemSameComponents(shown[CURRENT], lastShown[CURRENT]);
            if (initialised && rotated) {
                slideStartMs = Util.getMillis();
                leavingPrevious = lastShown[0];
                leavingCurrent = lastShown[CURRENT];
            }
            lastShown = new ItemStack[] {shown[0].copy(), shown[1].copy(), shown[2].copy()};
            initialised = true;
        }
        // Real time, so respawns and dimension changes (tick count resets) can't break it
        float progress = Mth.clamp((Util.getMillis() - slideStartMs) / (float) SLIDE_MS, 0.0F, 1.0F);
        float remaining = 1.0F - progress;
        return remaining * remaining * remaining; // ease out
    }

    // An arrow at a fractional strip position, sized and faded between the slots either side
    private static void drawArrow(GuiGraphicsExtractor graphics, Minecraft mc, Player player, ItemStack stack,
                                  float position, float fade, float left, int top, int seed) {
        if (stack.isEmpty() || position <= -1 || position >= SLOTS) return;
        float index = position + 1;
        int low = Mth.floor(index);
        float t = index - low;
        float scale = Mth.lerp(t, POSITION_SCALE[low], POSITION_SCALE[low + 1]);
        float alpha = Mth.lerp(t, POSITION_ALPHA[low], POSITION_ALPHA[low + 1]) * fade;
        float x = left + Mth.lerp(t, POSITION_CENTER[low], POSITION_CENTER[low + 1]);
        if (alpha <= 0.01F) return;

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, top + BOX / 2F);
        pose.scale(scale);
        if (alpha < 1.0F) {
            FadedItems.item(graphics, player, stack, -8, -8, seed, alpha);
            FadedItems.count(graphics, mc.font, stack, -8, -8, alpha);
        } else {
            graphics.item(player, stack, -8, -8, seed);
            graphics.itemDecorations(mc.font, stack, -8, -8);
        }
        pose.popMatrix();
    }

    // A single bordered slot (the hotbar's first slot plus its right edge); side slots shrunk and faded
    private static void drawFrame(GuiGraphicsExtractor graphics, int slot, float left, int top) {
        boolean current = slot == CURRENT;
        float scale = current ? 1.0F : SIDE_SCALE;
        int color = ARGB.white(current ? 1.0F : SIDE_ALPHA);
        float x = left + POSITION_CENTER[slot + 1] - BOX * scale / 2;
        float y = top + (BOX - BOX * scale) / 2; // side slots centred on the current one

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(scale);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, HOTBAR_WIDTH, HOTBAR_HEIGHT,
            0, 0, 0, 0, BOX - 1, HOTBAR_HEIGHT, color);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, HOTBAR_WIDTH, HOTBAR_HEIGHT,
            HOTBAR_WIDTH - 1, 0, BOX - 1, 0, 1, HOTBAR_HEIGHT, color);
        pose.popMatrix();
    }
}
