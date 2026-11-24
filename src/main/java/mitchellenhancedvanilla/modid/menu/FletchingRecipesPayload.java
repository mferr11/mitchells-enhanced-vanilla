package mitchellenhancedvanilla.modid.menu;

import java.util.List;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.recipe.FletchingRecipe;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;

// Unlocked fletching recipes for an open fletching table, in button order
public record FletchingRecipesPayload(int containerId, List<Entry> recipes) implements CustomPacketPayload {
    public static final Type<FletchingRecipesPayload> TYPE = new Type<>(MitchellEnhancedVanilla.id("fletching_recipes"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FletchingRecipesPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, FletchingRecipesPayload::containerId,
        Entry.CODEC.apply(ByteBufCodecs.list()), FletchingRecipesPayload::recipes,
        FletchingRecipesPayload::new);

    public record Entry(Ingredient tip, Ingredient shaft, Ingredient fletching, ItemStackTemplate result) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, Entry::tip,
            Ingredient.CONTENTS_STREAM_CODEC, Entry::shaft,
            Ingredient.CONTENTS_STREAM_CODEC, Entry::fletching,
            ItemStackTemplate.STREAM_CODEC, Entry::result,
            Entry::new);

        public static Entry of(FletchingRecipe recipe) {
            return new Entry(recipe.tip(), recipe.shaft(), recipe.fletching(), recipe.result());
        }

        public Ingredient ingredient(int slot) {
            return switch (slot) {
                case FletchingTableMenu.TIP_SLOT -> tip;
                case FletchingTableMenu.SHAFT_SLOT -> shaft;
                default -> fletching;
            };
        }
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }

    @Override
    public Type<FletchingRecipesPayload> type() {
        return TYPE;
    }
}
