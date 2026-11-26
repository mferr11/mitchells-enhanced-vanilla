package mitchellenhancedvanilla.modid.worldgen;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

// A brood nest: a web-choked cave carved deep underground (see BroodNestPiece for the shape and contents)
public class BroodNestStructure extends Structure {
    public static final MapCodec<BroodNestStructure> CODEC = simpleCodec(BroodNestStructure::new);

    // Centre height range, kept clear of the bedrock floor and the surface
    public static final int MIN_Y = -44;
    public static final int MAX_Y = 0;
    // Solid ground needed between the top of the nest and the surface (blocks)
    public static final int MIN_COVER = 12;

    public BroodNestStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getBlockX(8);
        int z = context.chunkPos().getBlockZ(8);
        int y = MIN_Y + context.random().nextInt(MAX_Y - MIN_Y + 1);
        int surface = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        if (surface - (y + BroodNestPiece.HALF_HEIGHT) < MIN_COVER) return Optional.empty();

        // Not in the Deep Dark or lush caves: they have their own inhabitants
        Holder<Biome> biome = context.biomeResolver().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z));
        if (biome.is(Biomes.DEEP_DARK) || biome.is(Biomes.LUSH_CAVES)) return Optional.empty();

        BlockPos centre = new BlockPos(x, y, z);
        long seed = context.random().nextLong();
        return Optional.of(new GenerationStub(centre, builder -> builder.addPiece(new BroodNestPiece(centre, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.BROOD_NEST;
    }
}
