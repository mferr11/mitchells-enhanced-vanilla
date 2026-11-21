package mitchellenhancedvanilla.modid.menu;

import mitchellenhancedvanilla.modid.ModTags;
import mitchellenhancedvanilla.modid.recipe.FletchingRecipeInput;
import mitchellenhancedvanilla.modid.recipe.ModRecipes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.item.ItemStack;
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

    // Client side
    public FletchingTableMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    // Server side
    public FletchingTableMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenus.FLETCHING_TABLE, containerId, inventory, access, createSlotDefinitions());
        this.level = inventory.player.level();
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
