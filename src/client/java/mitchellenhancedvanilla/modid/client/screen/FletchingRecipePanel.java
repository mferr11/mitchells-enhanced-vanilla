package mitchellenhancedvanilla.modid.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mitchellenhancedvanilla.modid.menu.FletchingRecipesPayload;
import mitchellenhancedvanilla.modid.menu.FletchingRecipesPayload.Entry;
import mitchellenhancedvanilla.modid.menu.FletchingTableMenu;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

// Recipe-book style side panel listing the player's unlocked fletching recipes
public class FletchingRecipePanel {
    // Layout copied from vanilla's RecipeBookComponent
    public static final int WIDTH = 147;
    public static final int HEIGHT = 166;
    private static final int OFFSET_X = 86;
    private static final int MIN_SIDE_BY_SIDE_WIDTH = 379; // narrower screens overlay the panel instead
    private static final int COLUMNS = 5;
    private static final int ROWS = 4;
    private static final int PER_PAGE = COLUMNS * ROWS;
    private static final int BUTTON_SIZE = 25;
    private static final int GRID_X = 11;
    private static final int GRID_Y = 31;
    private static final int PAGE_BACK_X = 38;
    private static final int PAGE_FORWARD_X = 93;
    private static final int PAGE_Y = 137;
    private static final int PAGE_BUTTON_WIDTH = 12;
    private static final int PAGE_BUTTON_HEIGHT = 17;

    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/recipe_book.png");
    private static final Identifier SLOT_CRAFTABLE = Identifier.withDefaultNamespace("recipe_book/slot_craftable");
    private static final Identifier SLOT_UNCRAFTABLE = Identifier.withDefaultNamespace("recipe_book/slot_uncraftable");
    private static final WidgetSprites PAGE_FORWARD = new WidgetSprites(
        Identifier.withDefaultNamespace("recipe_book/page_forward"),
        Identifier.withDefaultNamespace("recipe_book/page_forward_highlighted"));
    private static final WidgetSprites PAGE_BACKWARD = new WidgetSprites(
        Identifier.withDefaultNamespace("recipe_book/page_backward"),
        Identifier.withDefaultNamespace("recipe_book/page_backward_highlighted"));
    private static final Component SEARCH_HINT = Component.translatable("gui.recipebook.search_hint")
        .withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);

    // Latest recipes from the server, for this container id
    private static int receivedContainerId = -1;
    private static List<Entry> receivedRecipes = List.of();

    // Kept for the session, like the vanilla book's open state
    private static boolean open = false;
    private static String lastSearch = "";

    private final FletchingTableMenu menu;
    private final Minecraft minecraft;
    private EditBox searchBox;
    private int x;
    private int y;
    private boolean narrow;
    private int page;
    private Entry ghost; // shown faded in the slots after clicking a recipe you can't make

    public FletchingRecipePanel(FletchingTableMenu menu, Minecraft minecraft) {
        this.menu = menu;
        this.minecraft = minecraft;
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(FletchingRecipesPayload.TYPE, (payload, context) -> {
            receivedContainerId = payload.containerId();
            receivedRecipes = payload.recipes();
        });
    }

    // Read every time, as the payload may land just after the screen opens
    private List<Entry> recipes() {
        return menu.containerId == receivedContainerId ? receivedRecipes : List.of();
    }

    public void init(int screenWidth, int screenHeight, Font font) {
        narrow = screenWidth < MIN_SIDE_BY_SIDE_WIDTH;
        x = (screenWidth - WIDTH) / 2 - (narrow ? 0 : OFFSET_X);
        y = (screenHeight - HEIGHT) / 2;
        searchBox = new EditBox(font, x + 25, y + 13, 81, font.lineHeight + 5, Component.translatable("itemGroup.search"));
        searchBox.setMaxLength(50);
        searchBox.setBordered(false);
        searchBox.setTextColor(-1);
        searchBox.setHint(SEARCH_HINT);
        searchBox.setValue(lastSearch);
        searchBox.setVisible(open);
        searchBox.setResponder(text -> {
            lastSearch = text;
            page = 0;
        });
    }

    public EditBox searchBox() {
        return searchBox;
    }

    public boolean isOpen() {
        return open;
    }

    public void toggle() {
        open = !open;
        if (!open) ghost = null;
    }

    // Vanilla's updateScreenPosition: the container moves right to make room
    public int containerLeft(int screenWidth, int imageWidth) {
        return open && !narrow ? 177 + (screenWidth - imageWidth - 200) / 2 : (screenWidth - imageWidth) / 2;
    }

    public Entry ghost() {
        return ghost;
    }

    public void tick() {
        // Ghost goes once the player starts filling the slots
        for (int slot = 0; slot < 3; slot++) {
            if (menu.getSlot(slot).hasItem()) ghost = null;
        }
    }

    private List<Integer> filtered() {
        String query = searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        List<Entry> recipes = recipes();
        List<Integer> shown = new ArrayList<>();
        for (int i = 0; i < recipes.size(); i++) {
            String name = recipes.get(i).result().create().getHoverName().getString().toLowerCase(Locale.ROOT);
            if (query.isEmpty() || name.contains(query)) shown.add(i);
        }
        return shown;
    }

    private int pageCount(int shown) {
        return Math.max(1, (shown + PER_PAGE - 1) / PER_PAGE);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return open && mouseX >= x && mouseX < x + WIDTH && mouseY >= y && mouseY < y + HEIGHT;
    }

    // Each ingredient in the input slots or the main inventory
    private boolean[] haveIngredients(Entry entry) {
        boolean[] have = new boolean[3];
        Inventory inventory = minecraft.player.getInventory();
        for (int slot = 0; slot < 3; slot++) {
            Ingredient ingredient = entry.ingredient(slot);
            have[slot] = ingredient.test(menu.getSlot(slot).getItem());
            for (int i = 0; i < Inventory.INVENTORY_SIZE && !have[slot]; i++) {
                have[slot] = ingredient.test(inventory.getItem(i));
            }
        }
        return have;
    }

    private boolean canCraft(Entry entry) {
        for (boolean have : haveIngredients(entry)) {
            if (!have) return false;
        }
        return true;
    }

    public void extract(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (!open) return;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 1, 1, WIDTH, HEIGHT, 256, 256);
        searchBox.extractRenderState(graphics, mouseX, mouseY, partialTick);

        List<Integer> shown = filtered();
        page = Math.min(page, pageCount(shown.size()) - 1);
        List<Entry> recipes = recipes();
        for (int k = 0; k < PER_PAGE; k++) {
            int index = page * PER_PAGE + k;
            if (index >= shown.size()) break;
            Entry entry = recipes.get(shown.get(index));
            int bx = buttonX(k);
            int by = buttonY(k);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, canCraft(entry) ? SLOT_CRAFTABLE : SLOT_UNCRAFTABLE,
                bx, by, BUTTON_SIZE, BUTTON_SIZE);
            ItemStack result = entry.result().create();
            graphics.item(result, bx + 4, by + 4);
            graphics.itemDecorations(minecraft.font, result, bx + 4, by + 4);
        }

        int pages = pageCount(shown.size());
        if (pages > 1) {
            String text = (page + 1) + "/" + pages;
            graphics.text(minecraft.font, text, x + 73 - minecraft.font.width(text) / 2, y + PAGE_Y + 4, -1);
            if (page > 0) drawPageButton(graphics, PAGE_BACKWARD, PAGE_BACK_X, mouseX, mouseY);
            if (page < pages - 1) drawPageButton(graphics, PAGE_FORWARD, PAGE_FORWARD_X, mouseX, mouseY);
        }
    }

    private void drawPageButton(GuiGraphicsExtractor graphics, WidgetSprites sprites, int offsetX, int mouseX, int mouseY) {
        boolean hovered = over(mouseX, mouseY, x + offsetX, y + PAGE_Y, PAGE_BUTTON_WIDTH, PAGE_BUTTON_HEIGHT);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprites.get(true, hovered),
            x + offsetX, y + PAGE_Y, PAGE_BUTTON_WIDTH, PAGE_BUTTON_HEIGHT);
    }

    public void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!open) return;
        int hovered = hoveredRecipe(mouseX, mouseY);
        if (hovered < 0) return;
        // Full item tooltip, as in the inventory (includes the arrow stats)
        graphics.setTooltipForNextFrame(minecraft.font, recipes().get(hovered).result().create(), mouseX, mouseY);
    }

    // Index into recipes() of the button under the mouse, or -1
    private int hoveredRecipe(double mouseX, double mouseY) {
        List<Integer> shown = filtered();
        for (int k = 0; k < PER_PAGE; k++) {
            int index = page * PER_PAGE + k;
            if (index >= shown.size()) break;
            if (over(mouseX, mouseY, buttonX(k), buttonY(k), BUTTON_SIZE, BUTTON_SIZE)) return shown.get(index);
        }
        return -1;
    }

    // Shift-click fills as many as possible, like the vanilla book
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick, Runnable focusSearch) {
        if (!open) return false;
        if (searchBox.isMouseOver(event.x(), event.y())) {
            searchBox.mouseClicked(event, doubleClick);
            focusSearch.run();
            return true;
        }
        searchBox.setFocused(false);
        if (!isMouseOver(event.x(), event.y())) return false;

        int pages = pageCount(filtered().size());
        if (page > 0 && over(event.x(), event.y(), x + PAGE_BACK_X, y + PAGE_Y, PAGE_BUTTON_WIDTH, PAGE_BUTTON_HEIGHT)) {
            page--;
            return true;
        }
        if (page < pages - 1 && over(event.x(), event.y(), x + PAGE_FORWARD_X, y + PAGE_Y, PAGE_BUTTON_WIDTH, PAGE_BUTTON_HEIGHT)) {
            page++;
            return true;
        }

        int index = hoveredRecipe(event.x(), event.y());
        if (index >= 0) {
            Entry entry = recipes().get(index);
            if (canCraft(entry)) {
                ghost = null;
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                    FletchingTableMenu.buttonId(index, event.hasShiftDown()));
            } else {
                ghost = entry;
            }
        }
        return true; // clicks inside the panel never reach the container
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (!isMouseOver(mouseX, mouseY)) return false;
        int pages = pageCount(filtered().size());
        page = Math.max(0, Math.min(pages - 1, page - (int) Math.signum(scrollY)));
        return true;
    }

    // Typing in the search box shouldn't trigger the inventory key and close the screen
    public boolean keyPressed(KeyEvent event) {
        if (!open || !searchBox.isFocused() || event.isEscape()) return false;
        searchBox.keyPressed(event);
        return true;
    }

    private int buttonX(int k) {
        return x + GRID_X + (k % COLUMNS) * BUTTON_SIZE;
    }

    private int buttonY(int k) {
        return y + GRID_Y + (k / COLUMNS) * BUTTON_SIZE;
    }

    private static boolean over(double mouseX, double mouseY, int left, int top, int width, int height) {
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }
}
