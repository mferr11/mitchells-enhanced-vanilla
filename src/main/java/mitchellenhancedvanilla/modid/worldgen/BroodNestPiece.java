package mitchellenhancedvanilla.modid.worldgen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.ModBlocks;
import mitchellenhancedvanilla.modid.block.BroodCobwebBlock;
import mitchellenhancedvanilla.modid.block.BroodEggBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

// The whole brood nest in one piece: a tall, noisy main chamber with 2-3 side pockets, carved out of the rock. Its walls are
// turned to Brood Stone with patches of Brood Silk Block, hung with permanent Brood Cobwebs, with clutches of Brood Eggs on
// the floor, 2-3 Brood Chests, 2-3 Brood Cocoons out in the pockets, and the Brood Heart block at the centre.
// Everything is worked out from the centre and a seed, so each chunk can carve and decorate its own slice independently
public class BroodNestPiece extends StructurePiece {
    public static final ResourceKey<LootTable> CHEST_LOOT = ResourceKey.create(Registries.LOOT_TABLE, MitchellEnhancedVanilla.id("chests/brood_nest"));

    // Reach of the bounding box from the centre (blocks); the shape always fits inside
    public static final int HALF_WIDTH = 17;
    public static final int HALF_HEIGHT = 12;

    // Main chamber radii: across (min plus a random extra), up to the ceiling, and down to the flatter floor
    private static final double MAIN_RADIUS = 6.5;
    private static final double MAIN_RADIUS_SPREAD = 1.5;
    private static final double MAIN_UP = 6.5;
    private static final double MAIN_UP_SPREAD = 1.5;
    private static final double MAIN_DOWN = 3.0;
    // Side pockets: how many, how far out, and their size
    private static final int POCKETS_MIN = 2;
    private static final int POCKETS_MAX = 3;
    private static final double POCKET_RADIUS = 3.0;
    private static final double POCKET_RADIUS_SPREAD = 1.5;
    // Wobble on the walls: noise scale and strength
    private static final double NOISE_SCALE = 0.13;
    private static final double NOISE_STRENGTH = 0.22;
    // Wall lining: how far into the rock it reaches (in shape density, about 1-2 blocks), where the outer part starts
    // to fade back into plain rock, and the scale and threshold of the noise that picks silk patches over Brood Stone
    private static final double LINING_DEPTH = 0.5;
    private static final double LINING_FADE = 0.3;
    private static final double SILK_SCALE = 0.18;
    private static final double SILK_THRESHOLD = 0.35;

    // Decoration odds per open cell: eggs on the floor, webs on the floor, ceiling and walls (never floating)
    private static final float EGG_CHANCE = 0.07F;
    private static final float FLOOR_WEB_CHANCE = 0.08F;
    private static final float CEILING_WEB_CHANCE = 0.45F;
    private static final float WALL_WEB_CHANCE = 0.3F;
    // Cocoons and chests per nest
    private static final int COCOONS_MIN = 2;
    private static final int COCOONS_MAX = 3;
    // Cocoons in the main chamber (when there aren't enough pockets) sit this far from the centre
    private static final double COCOON_DISTANCE = 4.5;
    private static final int CHESTS = 2;
    private static final float EXTRA_CHEST_CHANCE = 1.0F / 3.0F;
    // Chests sit this far from the centre, inside the main chamber, and at least this far from the heart, cocoons and each other
    private static final double CHEST_DISTANCE_MIN = 2.5;
    private static final double CHEST_DISTANCE_SPREAD = 2.5;
    private static final int CHEST_SPACING = 2;

    private final BlockPos centre;
    private final long seed;
    private final List<Blob> blobs = new ArrayList<>();
    private final Set<BlockPos> cocoons = new HashSet<>();
    private @Nullable BlockPos heart;
    private final Set<BlockPos> chests = new HashSet<>();
    private final SimplexNoise noise;
    private final SimplexNoise silkNoise;

    // An ellipsoid of open space; flatter below its centre than above, for a walkable floor
    private record Blob(double x, double y, double z, double radius, double up, double down) {
        double density(double px, double py, double pz) {
            double dx = (px - x) / radius;
            double dy = (py - y) / (py >= y ? up : down);
            double dz = (pz - z) / radius;
            return 1.0 - (dx * dx + dy * dy + dz * dz);
        }
    }

    public BroodNestPiece(BlockPos centre, long seed) {
        super(ModStructures.BROOD_NEST_PIECE, 0, new BoundingBox(
            centre.getX() - HALF_WIDTH, centre.getY() - HALF_HEIGHT, centre.getZ() - HALF_WIDTH,
            centre.getX() + HALF_WIDTH, centre.getY() + HALF_HEIGHT, centre.getZ() + HALF_WIDTH));
        this.centre = centre;
        this.seed = seed;
        this.noise = new SimplexNoise(RandomSource.create(seed ^ 0x5DEECE66DL));
        this.silkNoise = new SimplexNoise(RandomSource.create(seed ^ 0x2545F4914F6CDD1DL));
        layout();
    }

    public BroodNestPiece(CompoundTag tag) {
        super(ModStructures.BROOD_NEST_PIECE, tag);
        this.centre = new BlockPos(tag.getIntOr("CX", 0), tag.getIntOr("CY", 0), tag.getIntOr("CZ", 0));
        this.seed = tag.getLongOr("Seed", 0L);
        this.noise = new SimplexNoise(RandomSource.create(seed ^ 0x5DEECE66DL));
        this.silkNoise = new SimplexNoise(RandomSource.create(seed ^ 0x2545F4914F6CDD1DL));
        layout();
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CX", centre.getX());
        tag.putInt("CY", centre.getY());
        tag.putInt("CZ", centre.getZ());
        tag.putLong("Seed", seed);
    }

    // Rebuilds the shape, heart, cocoons and chests from the seed (the same on every chunk and after a reload)
    private void layout() {
        RandomSource random = RandomSource.create(seed);
        double mainRadius = MAIN_RADIUS + random.nextDouble() * MAIN_RADIUS_SPREAD;
        blobs.add(new Blob(centre.getX() + 0.5, centre.getY(), centre.getZ() + 0.5, mainRadius,
            MAIN_UP + random.nextDouble() * MAIN_UP_SPREAD, MAIN_DOWN));

        int pockets = POCKETS_MIN + random.nextInt(POCKETS_MAX - POCKETS_MIN + 1);
        double angle = random.nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < pockets; i++) {
            // Spread round the chamber, with some jitter so they don't look evenly spaced
            angle += Math.PI * 2.0 / pockets + (random.nextDouble() - 0.5) * 0.8;
            double radius = POCKET_RADIUS + random.nextDouble() * POCKET_RADIUS_SPREAD;
            double distance = mainRadius + radius * 0.5 + random.nextDouble() * 1.5;
            double dy = random.nextInt(3) - 1;
            blobs.add(new Blob(centre.getX() + 0.5 + Math.cos(angle) * distance, centre.getY() + dy, centre.getZ() + 0.5 + Math.sin(angle) * distance,
                radius, radius * 1.2, radius * 0.7));
        }

        // The heart in the middle; cocoons in the pockets, then round the main chamber if there aren't enough
        heart = floorAt(centre.getX(), centre.getZ());
        Set<BlockPos> taken = new HashSet<>();
        if (heart != null) taken.add(heart);
        int cocoonCount = COCOONS_MIN + random.nextInt(COCOONS_MAX - COCOONS_MIN + 1);
        for (Blob blob : blobs.subList(1, blobs.size())) {
            if (cocoons.size() >= cocoonCount) break;
            BlockPos floor = floorAt(Mth.floor(blob.x()), Mth.floor(blob.z()));
            if (floor != null && isClear(floor, taken)) cocoons.add(floor);
        }
        for (int attempt = 0; attempt < 8 && cocoons.size() < cocoonCount; attempt++) {
            double cocoonAngle = random.nextDouble() * Math.PI * 2.0;
            BlockPos floor = floorAt(centre.getX() + Mth.floor(Math.cos(cocoonAngle) * COCOON_DISTANCE),
                centre.getZ() + Mth.floor(Math.sin(cocoonAngle) * COCOON_DISTANCE));
            if (floor != null && isClear(floor, taken) && isClear(floor, cocoons)) cocoons.add(floor);
        }
        taken.addAll(cocoons);

        // Chests: spots on the main chamber floor clear of the hearts and each other (a few tries each)
        int chestCount = CHESTS + (random.nextFloat() < EXTRA_CHEST_CHANCE ? 1 : 0);
        for (int attempt = 0; attempt < chestCount * 6 && chests.size() < chestCount; attempt++) {
            double chestAngle = random.nextDouble() * Math.PI * 2.0;
            double distance = CHEST_DISTANCE_MIN + random.nextDouble() * CHEST_DISTANCE_SPREAD;
            BlockPos floor = floorAt(centre.getX() + Mth.floor(Math.cos(chestAngle) * distance), centre.getZ() + Mth.floor(Math.sin(chestAngle) * distance));
            if (floor != null && isClear(floor, taken) && isClear(floor, chests)) chests.add(floor);
        }
    }

    private static boolean isClear(BlockPos pos, Set<BlockPos> others) {
        for (BlockPos other : others) {
            if (Math.abs(other.getX() - pos.getX()) <= CHEST_SPACING && Math.abs(other.getZ() - pos.getZ()) <= CHEST_SPACING) return false;
        }
        return true;
    }

    // How far inside the shape this block is: above 0 open, just below 0 the walls
    private double shape(int x, int y, int z) {
        if (!boundingBox.isInside(x, y, z)) return Double.NEGATIVE_INFINITY;
        double cx = x + 0.5;
        double cy = y + 0.5;
        double cz = z + 0.5;
        double density = Double.NEGATIVE_INFINITY;
        for (Blob blob : blobs) density = Math.max(density, blob.density(cx, cy, cz));
        return density + noise.get(cx * NOISE_SCALE, cy * NOISE_SCALE, cz * NOISE_SCALE) * NOISE_STRENGTH;
    }

    // Open (carved) at this block
    private boolean isInside(int x, int y, int z) {
        return shape(x, y, z) > 0.0;
    }

    // Floor height of the open column at x, z nearest the centre's height, or null if the column is solid there
    private @Nullable BlockPos floorAt(int x, int z) {
        for (int y = centre.getY() + HALF_HEIGHT; y > centre.getY() - HALF_HEIGHT; y--) {
            if (isInside(x, y, z) && !isInside(x, y - 1, z)) return new BlockPos(x, y, z);
        }
        return null;
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        int minX = Math.max(boundingBox.minX(), chunkBB.minX());
        int maxX = Math.min(boundingBox.maxX(), chunkBB.maxX());
        int minY = Math.max(boundingBox.minY(), Math.max(chunkBB.minY(), level.getMinY() + 5));
        int maxY = Math.min(boundingBox.maxY(), chunkBB.maxY());
        int minZ = Math.max(boundingBox.minZ(), chunkBB.minZ());
        int maxZ = Math.min(boundingBox.maxZ(), chunkBB.maxZ());
        if (minX > maxX || minY > maxY || minZ > maxZ) return;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        // Carve, and line the walls (sealing any water or lava there so the nest doesn't flood)
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    pos.set(x, y, z);
                    BlockState existing = level.getBlockState(pos);
                    if (existing.is(Blocks.BEDROCK)) continue;
                    double shape = shape(x, y, z);
                    if (shape > 0.0) {
                        level.setBlock(pos, CAVE_AIR, 2);
                    } else if (shape > -LINING_DEPTH) {
                        line(level, pos, existing, shape);
                    }
                }
            }
        }

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if (!isInside(x, y, z)) continue;
                    pos.set(x, y, z);
                    if (pos.equals(heart)) {
                        level.setBlock(pos, ModBlocks.BROOD_HEART.defaultBlockState(), 2);
                    } else if (cocoons.contains(pos)) {
                        level.setBlock(pos, ModBlocks.BROOD_COCOON.defaultBlockState(), 2);
                        level.scheduleTick(pos.immutable(), ModBlocks.BROOD_COCOON, 20);
                    } else if (chests.contains(pos)) {
                        placeChest(level, random, pos.immutable());
                    } else {
                        decorate(level, pos);
                    }
                }
            }
        }
    }

    // Rock, dirt and gravel in the walls become Brood Stone, with patches of Brood Silk Block; ores are left as they are.
    // Water and lava there are sealed. The outer part thins out into the plain rock around it
    private void line(WorldGenLevel level, BlockPos.MutableBlockPos pos, BlockState existing, double shape) {
        boolean fluid = !level.getFluidState(pos).isEmpty();
        boolean rock = existing.is(BlockTags.BASE_STONE_OVERWORLD) || existing.is(BlockTags.DIRT) || existing.is(Blocks.GRAVEL);
        if (!fluid && !rock) return;
        if (!fluid && shape < -LINING_FADE && RandomSource.create(seed ^ Mth.getSeed(pos)).nextBoolean()) return;
        boolean silk = silkNoise.get(pos.getX() * SILK_SCALE, pos.getY() * SILK_SCALE, pos.getZ() * SILK_SCALE) > SILK_THRESHOLD;
        level.setBlock(pos, (silk ? ModBlocks.BROOD_SILK_BLOCK : ModBlocks.BROOD_STONE).defaultBlockState(), 2);
    }

    // Eggs or webs depending on what the open cell rests against; random but the same however the chunks are generated
    private void decorate(WorldGenLevel level, BlockPos.MutableBlockPos pos) {
        RandomSource random = RandomSource.create(seed ^ Mth.getSeed(pos));
        float roll = random.nextFloat();
        boolean onFloor = isSupport(level, pos, Direction.DOWN);
        boolean underCeiling = isSupport(level, pos, Direction.UP);
        boolean byWall = isSupport(level, pos, Direction.NORTH) || isSupport(level, pos, Direction.SOUTH)
            || isSupport(level, pos, Direction.EAST) || isSupport(level, pos, Direction.WEST);

        BlockState placed = null;
        if (onFloor) {
            if (roll < EGG_CHANCE) {
                placed = ((BroodEggBlock) ModBlocks.BROOD_EGG).withRandomFacing(random).setValue(BroodEggBlock.EGGS, 1 + random.nextInt(4));
            } else if (roll < EGG_CHANCE + FLOOR_WEB_CHANCE) {
                placed = web();
            }
        } else if (underCeiling) {
            if (roll < CEILING_WEB_CHANCE) placed = web();
        } else if (byWall) {
            if (roll < WALL_WEB_CHANCE) placed = web();
        }
        if (placed != null) level.setBlock(pos, placed, 2);
    }

    // A solid block on that side, outside the carved space (so a cave cutting through the edge doesn't count)
    private boolean isSupport(WorldGenLevel level, BlockPos pos, Direction side) {
        BlockPos next = pos.relative(side);
        return !isInside(next.getX(), next.getY(), next.getZ())
            && level.getBlockState(next).isFaceSturdy(level, next, side.getOpposite());
    }

    private static BlockState web() {
        return ((BroodCobwebBlock) ModBlocks.BROOD_COBWEB).withLifetime(BroodCobwebBlock.Lifetime.PERMANENT);
    }

    // A Brood Chest facing the heart, filled from the nest loot table when first opened
    private void placeChest(WorldGenLevel level, RandomSource random, BlockPos pos) {
        Direction facing = Direction.getApproximateNearest(centre.getX() - pos.getX(), 0.0, centre.getZ() - pos.getZ());
        if (facing.getAxis().isVertical()) facing = Direction.NORTH;
        level.setBlock(pos, ModBlocks.BROOD_CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
        RandomizableContainer.setBlockEntityLootTable(level, random, pos, CHEST_LOOT);
    }
}
