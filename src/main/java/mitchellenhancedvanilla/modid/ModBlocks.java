package mitchellenhancedvanilla.modid;

import java.util.Set;
import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import mitchellenhancedvanilla.modid.block.ModChestBlock;
import mitchellenhancedvanilla.modid.block.BroodCobwebBlock;
import mitchellenhancedvanilla.modid.block.entity.ModChestBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
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

    // Vanilla cobweb's settings, but soft enough to cut free with anything. Only permanent webs drop anything
    public static final Block BROOD_COBWEB = register("brood_cobweb", BroodCobwebBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).sound(SoundType.COBWEB).forceSolidOn().noCollision()
            .strength(0.5F).pushReaction(PushReaction.POPPED));

    // Barrow ruins chest: a plain chest with its own look
    public static final Block BARROW_CHEST = register("barrow_chest",
        p -> new ModChestBlock(MitchellEnhancedVanilla.id("barrow_chest"), false, p), chestProperties(2.5F));
    // Brood nest chest: locked until the Barrow King is beaten, and blast-proof so it can't be blown open
    public static final Block BROOD_CHEST = register("brood_chest",
        p -> new ModChestBlock(MitchellEnhancedVanilla.id("brood_chest"), true, p), chestProperties(1200.0F));

    public static final BlockEntityType<ModChestBlockEntity> MOD_CHEST_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
        MitchellEnhancedVanilla.id("mod_chest"), new BlockEntityType<>(ModChestBlockEntity::new, Set.of(BARROW_CHEST, BROOD_CHEST)));

    // Vanilla chest's settings, with the given blast resistance (vanilla is 2.5)
    private static BlockBehaviour.Properties chestProperties(float explosionResistance) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F, explosionResistance)
            .sound(SoundType.WOOD).ignitedByLava();
    }

    private ModBlocks() {}

    public static void initialize() {
        // Uses vanilla's skull block entity, which only accepts listed blocks
        BlockEntityTypes.SKULL.addValidBlock(BARROW_KING_SKULL);
        BlockEntityTypes.SKULL.addValidBlock(BARROW_KING_WALL_SKULL);
        ModChestBlock.registerBreakLock();
    }
}
