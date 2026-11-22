package mitchellenhancedvanilla.modid.client.screen;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.menu.FletchingTableMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class FletchingTableScreen extends ItemCombinerScreen<FletchingTableMenu> {
    private static final Identifier TEXTURE =
        MitchellEnhancedVanilla.id("textures/gui/container/fletching_table.png");

    // Outline hints shown in empty input slots, like the smithing table's
    private static final Identifier TIP_HINT = MitchellEnhancedVanilla.id("container/slot/arrow_tip");
    private static final Identifier SHAFT_HINT = MitchellEnhancedVanilla.id("container/slot/stick");
    private static final Identifier FLETCHING_HINT = MitchellEnhancedVanilla.id("container/slot/feather");

    public FletchingTableScreen(FletchingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawHint(graphics, FletchingTableMenu.TIP_SLOT, TIP_HINT, FletchingTableMenu.TIP_Y);
        drawHint(graphics, FletchingTableMenu.SHAFT_SLOT, SHAFT_HINT, FletchingTableMenu.SHAFT_Y);
        drawHint(graphics, FletchingTableMenu.FLETCHING_SLOT, FLETCHING_HINT, FletchingTableMenu.FLETCHING_Y);
    }

    private void drawHint(GuiGraphicsExtractor graphics, int slot, Identifier sprite, int slotY) {
        if (menu.getSlot(slot).hasItem()) return;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, leftPos + FletchingTableMenu.INPUT_X, topPos + slotY, 16, 16);
    }

    @Override
    protected void extractErrorIcon(GuiGraphicsExtractor graphics, int x, int y) {
        // No error state: an invalid combination just shows an empty result slot.
    }
}
