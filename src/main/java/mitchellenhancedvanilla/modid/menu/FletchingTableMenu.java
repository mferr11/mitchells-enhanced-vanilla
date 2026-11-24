package mitchellenhancedvanilla.modid.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import mitchellenhancedvanilla.modid.ModTags;
import mitchellenhancedvanilla.modid.recipe.FletchingRecipe;
import mitchellenhancedvanilla.modid.recipe.FletchingRecipeInput;
import mitchellenhancedvanilla.modid.recipe.ModRecipes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

// Slot positions match the GUI texture
public class FletchingTableMenu extends ItemCombinerMenu {
    public static final int TIP_SLOT = 0;
    public static final int SHAFT_SLOT = 1;
    public static final int FLETCHING_SLOT = 2;
    public static final int RESULT_SLOT = 3;

    public static final int INPUT_X = 48;
    public static final int TIP_Y = 17;
    public static final int SHAFT_Y = 35;
    public static final int FLETCHING_Y = 53;

    private final Level level;
    // Server side: the player's unlocked recipes, in the order the client was sent them
    private final List<FletchingRecipe> unlockedRecipes;

    // Client side
    public FletchingTableMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    // Server side
    public FletchingTableMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenus.FLETCHING_TABLE, containerId, inventory, access, createSlotDefinitions());
        this.level = inventory.player.level();
        if (inventory.player instanceof ServerPlayer player && level instanceof ServerLevel serverLevel) {
            this.unlockedRecipes = findUnlockedRecipes(player, serverLevel);
            ServerPlayNetworking.send(player, new FletchingRecipesPayload(containerId,
                unlockedRecipes.stream().map(FletchingRecipesPayload.Entry::of).toList()));
        } else {
            this.unlockedRecipes = List.of();
        }
    }

    // Sorted like the fletching_tips tag, which is also the screen's tip hint order
    private static List<FletchingRecipe> findUnlockedRecipes(ServerPlayer player, ServerLevel level) {
        List<Holder<Item>> tipOrder = new ArrayList<>();
        BuiltInRegistries.ITEM.getTagOrEmpty(ModTags.FLETCHING_TIPS).forEach(tipOrder::add);
        return level.recipeAccess().getRecipes().stream()
            .filter(holder -> holder.value() instanceof FletchingRecipe)
            .filter(holder -> player.getRecipeBook().contains(holder.id()))
            .map(holder -> (FletchingRecipe) holder.value())
            .sorted(Comparator.comparingInt(recipe -> recipe.tip().items()
                .mapToInt(tipOrder::indexOf).filter(i -> i >= 0).findFirst().orElse(Integer.MAX_VALUE)))
            .toList();
    }

    // Recipe panel button: index of the recipe, low bit set to fill as many as possible
    public static int buttonId(int recipeIndex, boolean fill) {
        return recipeIndex * 2 + (fill ? 1 : 0);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        int index = id / 2;
        if (index < 0 || index >= unlockedRecipes.size()) return false;
        placeRecipe(player, unlockedRecipes.get(index), id % 2 == 1);
        return true;
    }

    // Moves one set of ingredients (or as many as fit) from the inventory into the input slots
    private void placeRecipe(Player player, FletchingRecipe recipe, boolean fill) {
        Inventory inventory = player.getInventory();
        Ingredient[] ingredients = {recipe.tip(), recipe.shaft(), recipe.fletching()};

        // Clicking the recipe already in the slots adds a set; a different recipe starts at one set
        boolean sameRecipe = true;
        int existingSets = Integer.MAX_VALUE;
        for (int slot = 0; slot < ingredients.length; slot++) {
            ItemStack current = inputSlots.getItem(slot);
            if (current.isEmpty() || !ingredients[slot].test(current)) {
                sameRecipe = false;
            } else {
                existingSets = Math.min(existingSets, current.getCount());
            }
        }
        int target = fill ? Integer.MAX_VALUE : sameRecipe ? existingSets + 1 : 1;

        // Choose each slot's item and check there's enough before moving anything
        ItemStack[] kinds = new ItemStack[ingredients.length];
        for (int slot = 0; slot < ingredients.length; slot++) {
            ItemStack current = inputSlots.getItem(slot);
            boolean keep = !current.isEmpty() && ingredients[slot].test(current);
            ItemStack kind = keep ? current : findInInventory(inventory, ingredients[slot]);
            if (kind.isEmpty()) return;
            kinds[slot] = kind.copyWithCount(1);
            int available = countInInventory(inventory, kind) + (keep ? current.getCount() : 0);
            target = Math.min(target, Math.min(available, kind.getMaxStackSize()));
        }
        if (target <= 0) return;

        // Ingredients from another recipe go back to the inventory
        for (int slot = 0; slot < ingredients.length; slot++) {
            ItemStack current = inputSlots.getItem(slot);
            if (current.isEmpty() || ingredients[slot].test(current)) continue;
            inventory.add(current);
            inputSlots.setItem(slot, current); // whatever did not fit stays put
            if (!current.isEmpty()) return;
        }

        // Top each slot up to the target; extras already there are left alone
        for (int slot = 0; slot < ingredients.length; slot++) {
            ItemStack current = inputSlots.getItem(slot);
            int missing = target - current.getCount();
            if (missing <= 0) continue;
            takeFromInventory(inventory, kinds[slot], missing);
            inputSlots.setItem(slot, kinds[slot].copyWithCount(current.getCount() + missing));
        }
    }

    private static ItemStack findInInventory(Inventory inventory, Ingredient ingredient) {
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && ingredient.test(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static int countInInventory(Inventory inventory, ItemStack kind) {
        int total = 0;
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            ItemStack stack = inventory.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, kind)) total += stack.getCount();
        }
        return total;
    }

    private static void takeFromInventory(Inventory inventory, ItemStack kind, int amount) {
        for (int i = 0; i < Inventory.INVENTORY_SIZE && amount > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!ItemStack.isSameItemSameComponents(stack, kind)) continue;
            int taken = Math.min(amount, stack.getCount());
            stack.shrink(taken);
            amount -= taken;
        }
        inventory.setChanged();
    }

    private static ItemCombinerMenuSlotDefinition createSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create()
            .withSlot(TIP_SLOT, INPUT_X, TIP_Y, stack -> stack.is(ModTags.FLETCHING_TIPS))
            .withSlot(SHAFT_SLOT, INPUT_X, SHAFT_Y, stack -> stack.is(ModTags.FLETCHING_SHAFTS))
            .withSlot(FLETCHING_SLOT, INPUT_X, FLETCHING_Y, stack -> stack.is(ModTags.FLETCHING_FLETCHINGS))
            .withResultSlot(RESULT_SLOT, 124, 35)
            .build();
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.is(Blocks.FLETCHING_TABLE);
    }

    private FletchingRecipeInput createRecipeInput() {
        return new FletchingRecipeInput(
            inputSlots.getItem(TIP_SLOT),
            inputSlots.getItem(SHAFT_SLOT),
            inputSlots.getItem(FLETCHING_SLOT));
    }

    @Override
    public void createResult() {
        // Recipes are server-only; the result syncs to the client
        if (!(level instanceof ServerLevel serverLevel)) return;

        FletchingRecipeInput input = createRecipeInput();
        ItemStack result = serverLevel.recipeAccess()
            .getRecipeFor(ModRecipes.FLETCHING, input, serverLevel)
            .map(holder -> holder.value().assemble(input))
            .filter(stack -> stack.isItemEnabled(serverLevel.enabledFeatures()))
            .orElse(ItemStack.EMPTY);
        resultSlots.setItem(0, result);
    }

    @Override
    protected void onTake(Player player, ItemStack stack) {
        stack.onCraftedBy(player, stack.getCount());
        shrinkStackInSlot(TIP_SLOT);
        shrinkStackInSlot(SHAFT_SLOT);
        shrinkStackInSlot(FLETCHING_SLOT);
        access.execute((level, pos) ->
            level.playSound(null, pos, SoundEvents.VILLAGER_WORK_FLETCHER, SoundSource.BLOCKS, 1.0F, 1.0F));
    }

    private void shrinkStackInSlot(int slot) {
        ItemStack stack = inputSlots.getItem(slot);
        if (!stack.isEmpty()) {
            stack.shrink(1);
            inputSlots.setItem(slot, stack);
        }
    }
}
