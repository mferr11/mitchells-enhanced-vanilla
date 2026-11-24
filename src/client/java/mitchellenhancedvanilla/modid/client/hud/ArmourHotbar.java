package mitchellenhancedvanilla.modid.client.hud;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// Equipped armour slots beside the hotbar, on the side opposite the offhand
public final class ArmourHotbar {
    private static final Identifier HOTBAR_SPRITE = Identifier.withDefaultNamespace("hud/hotbar");
    private static final int HOTBAR_WIDTH = 182;
    private static final int HOTBAR_HEIGHT = 22;
    private static final int HOTBAR_HALF = 91;
    private static final int SLOT_SIZE = 20;

    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final Identifier[] EMPTY_ICONS = {
        Identifier.withDefaultNamespace("container/slot/helmet"),
        Identifier.withDefaultNamespace("container/slot/chestplate"),
        Identifier.withDefaultNamespace("container/slot/leggings"),
        Identifier.withDefaultNamespace("container/slot/boots")
    };
    private static final float EMPTY_ICON_ALPHA = 0.35F;

    private static final int GAP = 7; // same gap as the offhand slot
    public static final int STRIP_WIDTH = SLOTS.length * SLOT_SIZE + 2;

    private ArmourHotbar() {}

    public static void register() {
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.HOTBAR,
            MitchellEnhancedVanilla.id("armour_hotbar"),
            ArmourHotbar::extract);
    }

    // How far the vanilla hotbar attack indicator must move to clear the strip
    public static int indicatorShift() {
        Player player = cameraPlayer();
        if (player == null || !isVisible(player)) return 0;
        int shift = GAP + STRIP_WIDTH;
        return player.getMainArm() == HumanoidArm.RIGHT ? shift : -shift;
    }

    private static Player cameraPlayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc.getCameraEntity() instanceof Player player && !player.isSpectator() ? player : null;
    }

    private static boolean isVisible(Player player) {
        for (EquipmentSlot slot : SLOTS) {
            if (!player.getItemBySlot(slot).isEmpty()) return true;
        }
        return false;
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Player player = cameraPlayer();
        if (player == null || !isVisible(player)) return;

        int center = graphics.guiWidth() / 2;
        int left = player.getMainArm() == HumanoidArm.RIGHT
            ? center + HOTBAR_HALF + GAP
            : center - HOTBAR_HALF - GAP - STRIP_WIDTH;
        int top = graphics.guiHeight() - HOTBAR_HEIGHT;

        // Slice of the vanilla hotbar: left border + 4 slots, then its right border
        int body = STRIP_WIDTH - 1;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, HOTBAR_WIDTH, HOTBAR_HEIGHT,
            0, 0, left, top, body, HOTBAR_HEIGHT);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, HOTBAR_WIDTH, HOTBAR_HEIGHT,
            HOTBAR_WIDTH - 1, 0, left + body, top, 1, HOTBAR_HEIGHT);

        Minecraft mc = Minecraft.getInstance();
        int itemY = top + 3;
        for (int i = 0; i < SLOTS.length; i++) {
            int itemX = left + 3 + i * SLOT_SIZE;
            ItemStack stack = player.getItemBySlot(SLOTS[i]);
            if (stack.isEmpty()) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EMPTY_ICONS[i], itemX, itemY, 16, 16, EMPTY_ICON_ALPHA);
            } else {
                graphics.item(player, stack, itemX, itemY, 100 + i); // seed past the hotbar's
                graphics.itemDecorations(mc.font, stack, itemX, itemY);
            }
        }
    }
}
