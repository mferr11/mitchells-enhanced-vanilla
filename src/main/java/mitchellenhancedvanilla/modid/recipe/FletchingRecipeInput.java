package mitchellenhancedvanilla.modid.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** The three fletching table input slots. */
public record FletchingRecipeInput(ItemStack tip, ItemStack shaft, ItemStack fletching) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> tip;
            case 1 -> shaft;
            case 2 -> fletching;
            default -> throw new IllegalArgumentException("Fletching recipe input has no slot " + index);
        };
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        return tip.isEmpty() && shaft.isEmpty() && fletching.isEmpty();
    }
}
