package mitchellenhancedvanilla.modid.recipe;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ModRecipes {
    public static final RecipeType<FletchingRecipe> FLETCHING = Registry.register(
        BuiltInRegistries.RECIPE_TYPE,
        MitchellEnhancedVanilla.id("fletching"),
        new RecipeType<FletchingRecipe>() {
            @Override
            public String toString() {
                return MitchellEnhancedVanilla.MOD_ID + ":fletching";
            }
        });

    public static final RecipeSerializer<FletchingRecipe> FLETCHING_SERIALIZER = Registry.register(
        BuiltInRegistries.RECIPE_SERIALIZER,
        MitchellEnhancedVanilla.id("fletching"),
        new RecipeSerializer<>(FletchingRecipe.MAP_CODEC, FletchingRecipe.STREAM_CODEC));

    public static void initialize() {
        // Loads this class so the recipe type and serializer above get registered.
    }
}
