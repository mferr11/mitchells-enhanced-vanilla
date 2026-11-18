package mitchellenhancedvanilla.modid.client.screen;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.menu.FletchingTableMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FletchingTableScreen extends ItemCombinerScreen<FletchingTableMenu> {
    private static final Identifier TEXTURE =
        MitchellEnhancedVanilla.id("textures/gui/container/fletching_table.png");

    // Faded hints shown in empty input slots
    private static final ItemStack TIP_HINT = new ItemStack(Items.FLINT);
    private static final ItemStack SHAFT_HINT = new ItemStack(Items.STICK);
    private static final ItemStack FLETCHING_HINT = new ItemStack(Items.FEATHER);
    private static final int HINT_FADE = 0xA08B8B8B; // slot colour drawn over the item to fade it

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

    private void drawHint(GuiGraphicsExtractor graphics, int slot, ItemStack hint, int slotY) {
        if (menu.getSlot(slot).hasItem()) return;
        int x = leftPos + FletchingTableMenu.INPUT_X;
        int y = topPos + slotY;
        graphics.fakeItem(hint, x, y);
        graphics.fill(x, y, x + 16, y + 16, HINT_FADE);
    }

    @Override
    protected void extractErrorIcon(GuiGraphicsExtractor graphics, int x, int y) {
        // No error state: an invalid combination just shows an empty result slot.
    }
}
