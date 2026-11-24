package mitchellenhancedvanilla.modid.client.screen;

import java.util.List;
import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.client.render.FadedItems;
import mitchellenhancedvanilla.modid.menu.FletchingRecipesPayload;
import mitchellenhancedvanilla.modid.menu.FletchingTableMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class FletchingTableScreen extends ItemCombinerScreen<FletchingTableMenu> {
    private static final Identifier TEXTURE =
        MitchellEnhancedVanilla.id("textures/gui/container/fletching_table.png");

    // Outline hints shown in empty input slots, like the smithing table's
    private static final Identifier SHAFT_HINT = MitchellEnhancedVanilla.id("container/slot/stick");
    private static final Identifier FLETCHING_HINT = MitchellEnhancedVanilla.id("container/slot/feather");

    // Tip slot cycles through every tip, in the fletching_tips tag order
    private static final List<Identifier> TIP_HINTS = List.of(
        MitchellEnhancedVanilla.id("container/slot/flint"),
        MitchellEnhancedVanilla.id("container/slot/iron_nugget"),
        MitchellEnhancedVanilla.id("container/slot/ice"),
        MitchellEnhancedVanilla.id("container/slot/copper_nugget"),
        Identifier.withDefaultNamespace("container/slot/amethyst_shard"),
        MitchellEnhancedVanilla.id("container/slot/gold_nugget"),
        MitchellEnhancedVanilla.id("container/slot/prismarine_crystals"),
        Identifier.withDefaultNamespace("container/slot/emerald"));

    // Recipe book toggle, where the crafting table has it
    private static final int BOOK_BUTTON_X = 5;
    private static final int BOOK_BUTTON_Y = 34;
    private static final int RESULT_X = 124;
    private static final int RESULT_Y = 35;
    private static final float GHOST_ALPHA = 0.4F;

    private final CyclingSlotBackground tipIcon = new CyclingSlotBackground(FletchingTableMenu.TIP_SLOT);
    private FletchingRecipePanel recipePanel;

    public FletchingTableScreen(FletchingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
    }

    @Override
    protected void init() {
        super.init();
        if (recipePanel == null) recipePanel = new FletchingRecipePanel(menu, minecraft);
        recipePanel.init(width, height, font);
        leftPos = recipePanel.containerLeft(width, imageWidth);
        addWidget(recipePanel.searchBox());
        addRenderableWidget(new ImageButton(leftPos + BOOK_BUTTON_X, topPos + BOOK_BUTTON_Y, 20, 18,
            RecipeBookComponent.RECIPE_BUTTON_SPRITES, button -> {
                recipePanel.toggle();
                rebuildWidgets();
            }));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        tipIcon.tick(TIP_HINTS);
        recipePanel.tick();
    }

    // Same layering as vanilla's recipe book screens
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractContents(graphics, mouseX, mouseY, partialTick);
        graphics.nextStratum();
        recipePanel.extract(graphics, mouseX, mouseY, partialTick);
        graphics.nextStratum();
        extractCarriedItem(graphics, mouseX, mouseY);
        extractTooltip(graphics, mouseX, mouseY);
        recipePanel.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        FletchingRecipesPayload.Entry ghost = recipePanel.ghost();
        if (ghost != null) {
            drawGhost(graphics, ghost);
            return;
        }
        tipIcon.extractRenderState(menu, graphics, partialTick, leftPos, topPos);
        drawHint(graphics, FletchingTableMenu.SHAFT_SLOT, SHAFT_HINT, FletchingTableMenu.SHAFT_Y);
        drawHint(graphics, FletchingTableMenu.FLETCHING_SLOT, FLETCHING_HINT, FletchingTableMenu.FLETCHING_Y);
    }

    private void drawHint(GuiGraphicsExtractor graphics, int slot, Identifier sprite, int slotY) {
        if (menu.getSlot(slot).hasItem()) return;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, leftPos + FletchingTableMenu.INPUT_X, topPos + slotY, 16, 16);
    }

    // Faded ingredients and result for a recipe the player is missing items for
    private void drawGhost(GuiGraphicsExtractor graphics, FletchingRecipesPayload.Entry ghost) {
        int[] slotYs = {FletchingTableMenu.TIP_Y, FletchingTableMenu.SHAFT_Y, FletchingTableMenu.FLETCHING_Y};
        for (int slot = 0; slot < slotYs.length; slot++) {
            if (menu.getSlot(slot).hasItem()) continue;
            int y = slotYs[slot];
            ghost.ingredient(slot).items().findFirst().ifPresent(item -> FadedItems.item(graphics, minecraft.player,
                new ItemStack(item), leftPos + FletchingTableMenu.INPUT_X, topPos + y, 0, GHOST_ALPHA));
        }
        ItemStack result = ghost.result().create();
        FadedItems.item(graphics, minecraft.player, result, leftPos + RESULT_X, topPos + RESULT_Y, 0, GHOST_ALPHA);
        FadedItems.count(graphics, font, result, leftPos + RESULT_X, topPos + RESULT_Y, GHOST_ALPHA);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (recipePanel.mouseClicked(event, doubleClick, () -> setFocused(recipePanel.searchBox()))) return true;
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (recipePanel.mouseScrolled(mouseX, mouseY, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (recipePanel.keyPressed(event)) return true;
        return super.keyPressed(event);
    }

    // Clicking the panel must not count as clicking outside (which drops the carried stack)
    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
        return super.hasClickedOutside(mouseX, mouseY, left, top) && !recipePanel.isMouseOver(mouseX, mouseY);
    }

    @Override
    protected void extractErrorIcon(GuiGraphicsExtractor graphics, int x, int y) {
        // No error state: an invalid combination just shows an empty result slot.
    }
}
