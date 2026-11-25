package mitchellenhancedvanilla.modid;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;

public final class ModBlocks {
    // Mob-head type for the King's skull; the client maps it to a model and texture
    public record SkullType(String name) implements SkullBlock.Type {
        public SkullType {
            // Lets vanilla's skull codec (block entities, item models) find it by name
            SkullBlock.Type.TYPES.put(name, this);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final SkullType BARROW_KING_SKULL_TYPE = new SkullType(MitchellEnhancedVanilla.MOD_ID + ":barrow_king");

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, MitchellEnhancedVanilla.id(name));
        return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
    }

    // Same settings as the vanilla skeleton skull
    public static final Block BARROW_KING_SKULL = register("barrow_king_skull",
        p -> new SkullBlock(BARROW_KING_SKULL_TYPE, p),
        BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.SKELETON).strength(1.0F)
            .pushReaction(PushReaction.POPPED).noOcclusion());

    // Wall version shares the floor skull's name and loot
    public static final Block BARROW_KING_WALL_SKULL = register("barrow_king_wall_skull",
        p -> new WallSkullBlock(BARROW_KING_SKULL_TYPE, p),
        BlockBehaviour.Properties.of().overrideLootTable(BARROW_KING_SKULL.getLootTable())
            .overrideDescription(BARROW_KING_SKULL.getDescriptionId()).strength(1.0F).pushReaction(PushReaction.POPPED));

    private ModBlocks() {}

    public static void initialize() {
        // Uses vanilla's skull block entity, which only accepts listed blocks
        BlockEntityTypes.SKULL.addValidBlock(BARROW_KING_SKULL);
        BlockEntityTypes.SKULL.addValidBlock(BARROW_KING_WALL_SKULL);
    }
}
