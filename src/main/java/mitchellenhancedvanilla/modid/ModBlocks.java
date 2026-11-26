package mitchellenhancedvanilla.modid;

import java.util.Set;
import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import mitchellenhancedvanilla.modid.block.ModChestBlock;
import mitchellenhancedvanilla.modid.block.BroodCobwebBlock;
import mitchellenhancedvanilla.modid.block.BroodEggBlock;
import mitchellenhancedvanilla.modid.block.BroodCocoonBlock;
import mitchellenhancedvanilla.modid.block.BroodHeartBlock;
import mitchellenhancedvanilla.modid.block.entity.BroodHeartBlockEntity;
import mitchellenhancedvanilla.modid.block.entity.ModChestBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.phys.AABB;
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

    // Vanilla's check for part-blocks (stairs, slabs) covering the camera
    private static final BlockBehaviour.StateArgumentPredicate<AABB> NEAR_PLANE_INTERSECTS_OUTLINE = (state, level, pos, nearPlaneBox) -> {
        for (AABB box : state.getOcclusionShape().toAabbs()) {
            if (box.move(pos).intersects(nearPlaneBox)) return true;
        }
        return false;
    };

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

    // Brood nest eggs, 1 to 4 to a block like turtle eggs; hatch larvae when broken or trampled (see the block class)
    public static final Block BROOD_EGG = register("brood_egg", BroodEggBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).sound(SoundType.FROGSPAWN).strength(0.3F)
            .noOcclusion().pushReaction(PushReaction.POPPED));

    // Brood nest walls: rock stained by the brood (stone's toughness, needs a pickaxe), and thick matted silk (soft as wool)
    public static final Block BROOD_STONE = register("brood_stone", Block::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.DEEPSLATE));
    // Building blocks cut from Brood Stone, as tough as it
    public static final Block POLISHED_BROOD_STONE = register("polished_brood_stone", Block::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.POLISHED_DEEPSLATE));
    public static final Block BROOD_STONE_BRICKS = register("brood_stone_bricks", Block::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.DEEPSLATE_BRICKS));
    public static final Block POLISHED_BROOD_STONE_STAIRS = registerStair("polished_brood_stone_stairs", POLISHED_BROOD_STONE);
    public static final Block POLISHED_BROOD_STONE_SLAB = registerSlab("polished_brood_stone_slab", POLISHED_BROOD_STONE);
    public static final Block POLISHED_BROOD_STONE_WALL = registerWall("polished_brood_stone_wall", POLISHED_BROOD_STONE);
    public static final Block BROOD_STONE_BRICK_STAIRS = registerStair("brood_stone_brick_stairs", BROOD_STONE_BRICKS);
    public static final Block BROOD_STONE_BRICK_SLAB = registerSlab("brood_stone_brick_slab", BROOD_STONE_BRICKS);
    public static final Block BROOD_STONE_BRICK_WALL = registerWall("brood_stone_brick_wall", BROOD_STONE_BRICKS);
    public static final Block BROOD_SILK_BLOCK = register("brood_silk_block", Block::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PINK).instrument(NoteBlockInstrument.GUITAR)
            .strength(0.8F).sound(SoundType.COBWEB).ignitedByLava());

    // Brood nest cocoon: bursts into Brood Spiders once the King has fallen (see the block class).
    // Unmineable until then; blast-proof so it can't be blown open early either
    public static final Block BROOD_COCOON = register("brood_cocoon", BroodCocoonBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).sound(SoundType.FROGSPAWN).strength(1.0F, 1200.0F)
            .noOcclusion().noLootTable().pushReaction(PushReaction.IMMOVEABLE).lightLevel(state -> 5));

    // Brood nest heart: a pulsing froglight-like block, blast-proof and sealed until the King falls
    public static final Block BROOD_HEART = register("brood_heart", BroodHeartBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).sound(SoundType.FROGLIGHT).strength(3.0F, 1200.0F)
            .pushReaction(PushReaction.IMMOVEABLE).lightLevel(state -> 5));

    // Barrow ruins chest: a plain chest with its own look
    public static final Block BARROW_CHEST = register("barrow_chest",
        p -> new ModChestBlock(MitchellEnhancedVanilla.id("barrow_chest"), false, p), chestProperties(2.5F));
    // Brood nest chest: locked until the Barrow King is beaten, and blast-proof so it can't be blown open
    public static final Block BROOD_CHEST = register("brood_chest",
        p -> new ModChestBlock(MitchellEnhancedVanilla.id("brood_chest"), true, p), chestProperties(1200.0F));

    public static final BlockEntityType<ModChestBlockEntity> MOD_CHEST_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
        MitchellEnhancedVanilla.id("mod_chest"), new BlockEntityType<>(ModChestBlockEntity::new, Set.of(BARROW_CHEST, BROOD_CHEST)));
    public static final BlockEntityType<BroodHeartBlockEntity> BROOD_HEART_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
        MitchellEnhancedVanilla.id("brood_heart"), new BlockEntityType<>(BroodHeartBlockEntity::new, Set.of(BROOD_HEART)));

    // Vanilla's stair, slab and wall settings: copies of the base block
    private static Block registerStair(String name, Block base) {
        return register(name, p -> new StairBlock(base.defaultBlockState(), p),
            BlockBehaviour.Properties.ofLegacyCopy(base).isViewBlocking(NEAR_PLANE_INTERSECTS_OUTLINE));
    }

    private static Block registerSlab(String name, Block base) {
        return register(name, SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(base).isViewBlocking(NEAR_PLANE_INTERSECTS_OUTLINE));
    }

    private static Block registerWall(String name, Block base) {
        return register(name, WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(base).forceSolidOn());
    }

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
