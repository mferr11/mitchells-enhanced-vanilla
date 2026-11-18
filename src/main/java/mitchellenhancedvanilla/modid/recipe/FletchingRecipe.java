package mitchellenhancedvanilla.modid.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * A fletching table recipe: one tip, one shaft and one fletching item make the result.
 *
 * JSON (data/<namespace>/recipe/...):
 * <pre>
 * {
 *   "type": "mitchells-enhanced-vanilla:fletching",
 *   "tip": "minecraft:flint",
 *   "shaft": "minecraft:stick",
 *   "fletching": "minecraft:feather",
 *   "result": { "id": "minecraft:arrow", "count": 8 }
 * }
 * </pre>
 * Each ingredient can also be a tag ("#minecraft:...") or a list of items.
 */
public class FletchingRecipe implements Recipe<FletchingRecipeInput> {
    public static final MapCodec<FletchingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
        Ingredient.CODEC.fieldOf("tip").forGetter(r -> r.tip),
        Ingredient.CODEC.fieldOf("shaft").forGetter(r -> r.shaft),
        Ingredient.CODEC.fieldOf("fletching").forGetter(r -> r.fletching),
        ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(instance, FletchingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FletchingRecipe> STREAM_CODEC = StreamCodec.composite(
        Recipe.CommonInfo.STREAM_CODEC, r -> r.commonInfo,
        Ingredient.CONTENTS_STREAM_CODEC, r -> r.tip,
        Ingredient.CONTENTS_STREAM_CODEC, r -> r.shaft,
        Ingredient.CONTENTS_STREAM_CODEC, r -> r.fletching,
        ItemStackTemplate.STREAM_CODEC, r -> r.result,
        FletchingRecipe::new);

    private final Recipe.CommonInfo commonInfo;
    private final Ingredient tip;
    private final Ingredient shaft;
    private final Ingredient fletching;
    private final ItemStackTemplate result;
    private PlacementInfo placementInfo;

    public FletchingRecipe(Recipe.CommonInfo commonInfo, Ingredient tip, Ingredient shaft, Ingredient fletching, ItemStackTemplate result) {
        this.commonInfo = commonInfo;
        this.tip = tip;
        this.shaft = shaft;
        this.fletching = fletching;
        this.result = result;
    }

    @Override
    public boolean matches(FletchingRecipeInput input, Level level) {
        return tip.test(input.tip()) && shaft.test(input.shaft()) && fletching.test(input.fletching());
    }

    @Override
    public ItemStack assemble(FletchingRecipeInput input) {
        return result.create();
    }

    // Keeps these recipes out of the recipe book, which has no fletching tab.
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return commonInfo.showNotification();
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<FletchingRecipe> getSerializer() {
        return ModRecipes.FLETCHING_SERIALIZER;
    }

    @Override
    public RecipeType<FletchingRecipe> getType() {
        return ModRecipes.FLETCHING;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (placementInfo == null) {
            placementInfo = PlacementInfo.create(List.of(tip, shaft, fletching));
        }
        return placementInfo;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        // Required, but unused because isSpecial() keeps these out of the recipe book.
        return RecipeBookCategories.CRAFTING_EQUIPMENT;
    }
}
