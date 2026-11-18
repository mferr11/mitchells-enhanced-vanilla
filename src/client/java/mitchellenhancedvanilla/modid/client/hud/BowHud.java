package mitchellenhancedvanilla.modid.client.hud;

import mitchellenhancedvanilla.modid.BowStats;
import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

/**
 * Bow HUD under the crosshair:
 *  - a draw bar on a dark track, filling in whole GUI pixels; turns gold with end ticks at full draw
 *  - the ammo icon and count, centered under the bar
 *  - the ammo's name, shown briefly under that row when the ammo type changes, then fading out
 */
public final class BowHud {
    private static final int CROSSHAIR_SIZE = 15; // vanilla crosshair sprite size

    // Bow strength bar
    private static final int BAR_WIDTH = 10;
    private static final int BAR_HEIGHT = 1;
    private static final int BAR_OFFSET_Y = 9;  // from crosshair center to top of bar

    // Ammo row
    private static final int AMMO_GAP = 3;      // bar -> ammo row
    private static final float ICON_SCALE = 0.75F; // items are 16px, so 12px
    private static final float COUNT_SCALE = 0.75F; // count text size relative to normal
    private static final int ICON_TEXT_GAP = 2;

    // Ammo name
    private static final int NAME_GAP = 3;           // ammo row -> name
    private static final float NAME_SCALE = 0.75F;   // text size relative to normal
    private static final long NAME_SHOW_MS = 750;   // fully visible for this long...
    private static final long NAME_FADE_MS = 500;    // ...then fades out over this long

    private static final int COLOR_TRACK = 0x50000000;   // dark track
    private static final int COLOR_FILL = 0x60C0C0C0;    // transparent grey while drawing
    private static final int COLOR_FULL = 0xFFFFFFFF;    // opaque white at full draw
    private static final int COLOR_COUNT = 0xFFFFFFFF;
    private static final int COLOR_NAME_RGB = 0xFFFFFF;  // white (alpha is added for the fade)

    // Last ammo shown, so we can tell when it changes
    private static ItemStack lastAmmo = ItemStack.EMPTY;
    private static long nameShownAt = Long.MIN_VALUE;

    private BowHud() {}

    public static void register() {
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.CROSSHAIR,
            MitchellEnhancedVanilla.id("bow_hud"),
            BowHud::extract);
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !player.isUsingItem()) return;

        ItemStack bow = player.getUseItem();
        if (!(bow.getItem() instanceof BowItem)) return;

        int ticks = player.getTicksUsingItem();

        boolean full = BowItem.getPowerForTime(ticks) >= 1.0F;
        float progress = full ? 1.0F : Math.min(ticks / BowStats.DRAW_TICKS, 1.0F);

        int centerX = (graphics.guiWidth() - CROSSHAIR_SIZE) / 2 + CROSSHAIR_SIZE / 2;
        int centerY = (graphics.guiHeight() - CROSSHAIR_SIZE) / 2 + CROSSHAIR_SIZE / 2;

        int barTop = centerY + BAR_OFFSET_Y;
        drawBar(graphics, centerX, barTop, progress, full);

        ItemStack ammo = player.getProjectile(bow);
        if (ammo.isEmpty()) return;

        int rowTop = barTop + BAR_HEIGHT + AMMO_GAP;
        int rowHeight = drawAmmoRow(graphics, mc.font, player, ammo, centerX, rowTop);

        if (!ItemStack.isSameItemSameComponents(ammo, lastAmmo)) {
            lastAmmo = ammo.copy();
            nameShownAt = Util.getMillis();
        }
        drawAmmoName(graphics, mc.font, ammo.getHoverName(), centerX, rowTop + rowHeight + NAME_GAP);
    }

    private static void drawBar(GuiGraphicsExtractor graphics, int centerX, int top, float progress, boolean full) {
        int left = centerX - BAR_WIDTH / 2;
        int right = left + BAR_WIDTH;
        int bottom = top + BAR_HEIGHT;

        graphics.fill(left, top, right, bottom, COLOR_TRACK);

        // Whole pixels only, like vanilla sprites
        int filled = (int) (BAR_WIDTH * progress);
        if (filled > 0) {
            graphics.fill(left, top, left + filled, bottom, full ? COLOR_FULL : COLOR_FILL);
        }
    }

    /** Draws the ammo icon and count centered on centerX. Returns the row's height. */
    private static int drawAmmoRow(GuiGraphicsExtractor graphics, Font font, LocalPlayer player, ItemStack ammo, int centerX, int top) {
        // Total of this exact ammo type (tipped arrows of different effects count separately).
        // 0 means the bow is using free creative-mode ammo, so no count is shown.
        int count = countInInventory(player.getInventory(), ammo);
        String countText = count > 0 ? String.valueOf(count) : "";

        int iconSize = Math.round(16 * ICON_SCALE);
        float countWidth = font.width(countText) * COUNT_SCALE;
        float rowWidth = iconSize + (countText.isEmpty() ? 0 : ICON_TEXT_GAP + countWidth);
        int left = Math.round(centerX - rowWidth / 2);

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(left, top);
        pose.scale(ICON_SCALE, ICON_SCALE);
        graphics.item(ammo, 0, 0);
        pose.popMatrix();

        if (!countText.isEmpty()) {
            // Vertically centered on the icon (lineHeight includes 1px below the glyphs)
            float countHeight = (font.lineHeight - 1) * COUNT_SCALE;
            pose.pushMatrix();
            pose.translate(left + iconSize + ICON_TEXT_GAP, top + (iconSize - countHeight) / 2);
            pose.scale(COUNT_SCALE, COUNT_SCALE);
            graphics.text(font, countText, 0, 0, COLOR_COUNT, true);
            pose.popMatrix();
        }
        return iconSize;
    }

    private static void drawAmmoName(GuiGraphicsExtractor graphics, Font font, Component name, int centerX, int top) {
        long elapsed = Util.getMillis() - nameShownAt;
        if (elapsed >= NAME_SHOW_MS + NAME_FADE_MS) return;

        float alpha = elapsed <= NAME_SHOW_MS ? 1.0F : 1.0F - (elapsed - NAME_SHOW_MS) / (float) NAME_FADE_MS;
        int a = Math.round(alpha * 255);
        if (a < 8) return; // very low alpha text can render as fully opaque

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(centerX - font.width(name) * NAME_SCALE / 2, top);
        pose.scale(NAME_SCALE, NAME_SCALE);
        graphics.text(font, name, 0, 0, (a << 24) | COLOR_NAME_RGB, true);
        pose.popMatrix();
    }

    private static int countInInventory(Inventory inventory, ItemStack match) {
        int total = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, match)) {
                total += stack.getCount();
            }
        }
        return total;
    }
}
