package mitchellenhancedvanilla.modid.worldgen;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

// Code-driven structures; their placement and biomes live in data/.../worldgen
public final class ModStructures {
    public static final StructureType<BroodNestStructure> BROOD_NEST = Registry.register(BuiltInRegistries.STRUCTURE_TYPE,
        MitchellEnhancedVanilla.id("brood_nest"), () -> BroodNestStructure.CODEC);
    public static final StructurePieceType BROOD_NEST_PIECE = Registry.register(BuiltInRegistries.STRUCTURE_PIECE,
        MitchellEnhancedVanilla.id("brood_nest"), (StructurePieceType.ContextlessType) BroodNestPiece::new);

    // The configured structure (worldgen/structure/brood_nest.json)
    public static final ResourceKey<Structure> BROOD_NEST_KEY = ResourceKey.create(Registries.STRUCTURE, MitchellEnhancedVanilla.id("brood_nest"));

    private ModStructures() {}

    public static void initialize() {
    }
}
