package com.example.mod.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.example.mod.ExampleMod;
import com.example.mod.world.biome.LayeredBiomeSource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
// import net.minecraft.core.registries.Registries; // No longer directly used
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.ChunkPos;


import java.util.List;
// import java.util.ArrayList; // No longer explicitly used
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
// import java.util.Optional; // No longer explicitly used

public class LayeredBiomeChunkGenerator extends ChunkGenerator {

    public static final Codec<LayeredBiomeChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
        ).apply(instance, instance.stable(LayeredBiomeChunkGenerator::new))
    );

    public static void register() {
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, new ResourceLocation(ExampleMod.MODID, "layered_chunk_generator"), CODEC);
    }

    public LayeredBiomeChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
    
    private BlockState getBiomeSpecificBlock(Holder<Biome> biome, String type) {
        if (biome == null || !biome.isBound()) return Blocks.STONE.defaultBlockState();
        if ("top".equals(type)) {
            return biome.value().getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial();
        } else { // "filler" or default
            return biome.value().getGenerationSettings().getSurfaceBuilderConfig().getUnderMaterial();
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int minChunkY = chunk.getMinBuildHeight(); // Y-coordinate of the bottom of the chunk (absolute)
        int maxChunkY = chunk.getMaxBuildHeight(); // Y-coordinate of the top of the chunk (exclusive, absolute)

        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        Climate.Sampler climateSampler = randomState.sampler(); 

        LayeredBiomeSource actualBiomeSource;
        if (this.biomeSource instanceof LayeredBiomeSource) {
            actualBiomeSource = (LayeredBiomeSource) this.biomeSource;
        } else {
            throw new IllegalStateException("LayeredBiomeChunkGenerator requires a LayeredBiomeSource. Found: " + this.biomeSource.getClass().getName());
        }
        
        int layerHeight = actualBiomeSource.getLayerHeight();
        int layerCount = actualBiomeSource.getLayerCount();
        // Max Y (exclusive) for actual layer content. Assumes dimension min_y is 0.
        int maxContentYExclusive = 0 + (layerCount * layerHeight);

        for (int relX = 0; relX < 16; relX++) {
            for (int relZ = 0; relZ < 16; relZ++) {
                int absX = chunk.getPos().getMinBlockX() + relX; 
                int absZ = chunk.getPos().getMinBlockZ() + relZ; 
                
                // Place bedrock at absolute Y=0 if this chunk contains it.
                // Our dimension's min_y is 0.
                if (minChunkY == 0) { 
                    chunk.setBlockState(mutable.set(relX, 0, relZ), bedrock, false); // relY for Y=0 is 0 if minChunkY is 0
                }

                // Iterate through Y levels relevant to this chunk for layered content
                for (int absY = Math.max(1, minChunkY) ; absY < Math.min(maxContentYExclusive, maxChunkY); absY++) { 
                    int relY = absY - minChunkY; // Y relative to chunk start
                    Holder<Biome> currentBiomeHolder = actualBiomeSource.getNoiseBiome(absX >> 2, absY >> 2, absZ >> 2, climateSampler);
                    
                    BlockState blockToSet;
                    // Determine layer properties based on absolute Y (relative to dimension min_y=0)
                    int layerIndex = Math.floorDiv(absY, layerHeight); 
                    int layerBaseAbsY = layerIndex * layerHeight;
                    int layerTopAbsY = layerBaseAbsY + layerHeight - 1;

                    if (absY == layerTopAbsY) { 
                         blockToSet = getBiomeSpecificBlock(currentBiomeHolder, "top");
                    } else if (absY > layerTopAbsY - 3 && absY < layerTopAbsY) { 
                         blockToSet = getBiomeSpecificBlock(currentBiomeHolder, "top"); 
                    } else { 
                        blockToSet = getBiomeSpecificBlock(currentBiomeHolder, "filler");
                    }
                    chunk.setBlockState(mutable.set(relX, relY, relZ), blockToSet, false);
                }
                 // Fill with air above defined content layers, up to chunk's max Y
                for (int absY = Math.max(maxContentYExclusive, minChunkY); absY < maxChunkY ; absY++) {
                     int relY = absY - minChunkY;
                     chunk.setBlockState(mutable.set(relX, relY, relZ), Blocks.AIR.defaultBlockState(), false);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }
    
    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState randomState, ChunkAccess chunk) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        int minChunkY = chunk.getMinBuildHeight();

        for (int relX = 0; relX < 16; ++relX) {
            for (int relZ = 0; relZ < 16; ++relZ) {
                int topNonAirAbsY = minChunkY -1; 
                for (int absY = chunk.getMaxBuildHeight() -1; absY >= minChunkY; --absY) {
                    int relY = absY - minChunkY;
                    if (!chunk.getBlockState(mutablePos.set(relX, relY, relZ)).isAir()) { 
                        topNonAirAbsY = absY;
                        break;
                    }
                }
                if(topNonAirAbsY >= minChunkY) { 
                    int relTopY = topNonAirAbsY - minChunkY;
                    BlockState topState = chunk.getBlockState(mutablePos.set(relX, relTopY, relZ)); 
                    // Heightmaps expect absolute X,Z relative to chunk origin (0-15), and absolute Y
                    oceanFloor.update(relX, topNonAirAbsY, relZ, topState); 
                    worldSurface.update(relX, topNonAirAbsY, relZ, topState);
                }
            }
        }
    }

    @Override
    public void applyBiomeDecoration(WorldGenRegion region, ChunkAccess chunk, StructureManager structureManager, RandomState randomState) {
        // Call super to ensure vanilla decoration logic (like structures, if any) can run.
        // For a completely custom generator, this might be omitted or selectively called.
        // However, ore generation is typically a biome decoration step.
        // super.applyBiomeDecoration(region, chunk, structureManager, randomState); 

        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        if (!(this.biomeSource instanceof LayeredBiomeSource)) {
            // If not our biome source, fall back to default behavior (which might be super call or nothing)
            super.applyBiomeDecoration(region, chunk, structureManager, randomState);
            return;
        }
        LayeredBiomeSource layeredBiomeSource = (LayeredBiomeSource) this.biomeSource;
        int layerHeight = layeredBiomeSource.getLayerHeight();
        
        // Get biomes present in the Y-range of this specific chunk
        List<Holder<Biome>> distinctBiomesInChunk = layeredBiomeSource.getBiomesWithinYRange(chunk.getMinBuildHeight(), chunk.getMaxBuildHeight() -1);

        for (Holder<Biome> biomeHolder : distinctBiomesInChunk) {
            if (!biomeHolder.isBound()) continue;

            int biomeLayerIndex = layeredBiomeSource.getLayerIndexForBiome(biomeHolder);
            if (biomeLayerIndex == -1) continue; // Should not happen if getBiomesWithinYRange is correct

            // Calculate the global Y range for this specific biome layer
            // Assuming dimension min_y is 0 for layer calculations.
            int globalLayerMinY = 0 + biomeLayerIndex * layerHeight; 
            int globalLayerMaxY = globalLayerMinY + layerHeight -1; // Inclusive

            // Determine the actual Y range for decoration within this chunk for this layer
            int decorationMinY = Math.max(globalLayerMinY, chunk.getMinBuildHeight());
            int decorationMaxY = Math.min(globalLayerMaxY, chunk.getMaxBuildHeight() - 1);

            if (decorationMaxY < decorationMinY) { // Layer is not present in this chunk's Y range
                continue;
            }
            
            List<HolderSet<PlacedFeature>> featuresByStep = biomeHolder.value().getGenerationSettings().features();
            // We are interested in UNDERGROUND_ORES, and potentially others like VEGETAL_DECORATION if applicable
            int undergroundOresStepOrdinal = GenerationStep.Decoration.UNDERGROUND_ORES.ordinal();

            if (featuresByStep.size() <= undergroundOresStepOrdinal) {
                continue; // No ore features defined for this biome
            }
            HolderSet<PlacedFeature> oreFeatures = featuresByStep.get(undergroundOresStepOrdinal);

            for (Holder<PlacedFeature> placedFeatureHolder : oreFeatures) {
                if (placedFeatureHolder.isBound()) {
                    PlacedFeature placedFeature = placedFeatureHolder.value();
                    // Attempt to place the feature.
                    // The PlacedFeature itself has rules about where it can generate (e.g. height ranges).
                    // We are essentially telling it: "Try to generate within this chunk, respecting your own rules,
                    // but conceptually, we are focusing on the Y-range of this biome layer."
                    // A simple strategy: pick a Y in the middle of the layer's presence in this chunk.
                    int placementAttemptY = decorationMinY + (decorationMaxY - decorationMinY) / 2;
                    
                    // Place feature at a sample position within the chunk and layer
                    // `placeWithBiomeCheck` will perform the actual placement attempts based on feature config.
                    // It will internally use the biome at the placement position.
                    mutablePos.set(chunkPos.getMiddleBlockX(), placementAttemptY, chunkPos.getMiddleBlockZ());
                    placedFeature.placeWithBiomeCheck(region, this, randomState, mutablePos);
                }
            }
        }
    }
    
    @Override
    public int getBaseHeight(int absX, int absZ, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
        LayeredBiomeSource actualBiomeSource;
        if (this.biomeSource instanceof LayeredBiomeSource) {
            actualBiomeSource = (LayeredBiomeSource) this.biomeSource;
        } else {
            return level.getMinBuildHeight(); 
        }

        int layerHeight = actualBiomeSource.getLayerHeight();
        int layerCount = actualBiomeSource.getLayerCount();
        int dimMinY = 0; // Our dimension's min_y is 0
        
        // Highest Y coordinate that can have a solid block from layers (inclusive)
        int maxSolidYFromLayers = dimMinY + (layerCount * layerHeight) -1;
        if (layerCount == 0) maxSolidYFromLayers = dimMinY -1; 

        if (type == Heightmap.Types.WORLD_SURFACE_WG || type == Heightmap.Types.OCEAN_FLOOR_WG) {
             // Return Y of the highest solid block (not Y+1 for this method).
             // Ensure it's within the dimension's bounds.
             return Math.min(dimMinY + level.getHeight() -1, maxSolidYFromLayers);
        }
        return dimMinY;
    }

    @Override
    public NoiseColumn getBaseColumn(int absX, int absZ, LevelHeightAccessor heightAccessor, RandomState randomState) {
        BlockState[] column = new BlockState[heightAccessor.getHeight()];
        int dimMinY = heightAccessor.getMinBuildHeight(); // Should be 0 for our dimension
        Climate.Sampler climateSampler = randomState.sampler();
        LayeredBiomeSource actualBiomeSource;
        if (this.biomeSource instanceof LayeredBiomeSource) {
            actualBiomeSource = (LayeredBiomeSource) this.biomeSource;
        } else {
            throw new IllegalStateException("LayeredBiomeChunkGenerator requires LayeredBiomeSource for getBaseColumn.");
        }
        
        int layerHeight = actualBiomeSource.getLayerHeight();
        int layerCount = actualBiomeSource.getLayerCount();
        // Max Y (exclusive) for actual layer content, relative to dimension min_y=0
        int maxContentYExclusive = dimMinY + (layerCount * layerHeight); 

        for (int yIdx = 0; yIdx < column.length; ++yIdx) {
            int currentAbsY = dimMinY + yIdx;
            BlockState blockToSet;

            if (currentAbsY >= maxContentYExclusive) { 
                blockToSet = Blocks.AIR.defaultBlockState();
            } else if (currentAbsY == dimMinY) { // Bedrock at the very bottom of the dimension
                blockToSet = Blocks.BEDROCK.defaultBlockState();
            } else { 
                Holder<Biome> currentBiomeHolder = actualBiomeSource.getNoiseBiome(absX >> 2, currentAbsY >> 2, absZ >> 2, climateSampler);
                if (currentBiomeHolder.isBound()) {
                    // Layer index based on absolute Y relative to dimension's min_y
                    int layerIndex = Math.floorDiv(currentAbsY - dimMinY, layerHeight); 
                    int layerBaseAbsY = dimMinY + (layerIndex * layerHeight);
                    int layerTopAbsY = layerBaseAbsY + layerHeight - 1;

                    if (currentAbsY == layerTopAbsY) {
                         blockToSet = getBiomeSpecificBlock(currentBiomeHolder, "top");
                    } else if (currentAbsY > layerTopAbsY - 3 && currentAbsY < layerTopAbsY) {
                         blockToSet = getBiomeSpecificBlock(currentBiomeHolder, "top");
                    } else {
                        blockToSet = getBiomeSpecificBlock(currentBiomeHolder, "filler");
                    }
                } else {
                    blockToSet = Blocks.STONE.defaultBlockState(); 
                }
            }
            column[yIdx] = blockToSet;
        }
        return new NoiseColumn(dimMinY, column);
    }
    
    // Unchanged methods from previous steps
    @Override public void applyCarvers(WorldGenRegion region, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving carver) {}
    @Override public void spawnOriginalMobs(WorldGenRegion region) {}
    @Override public int getGenDepth() { return getMaxHeight() - getMinHeight(); }
    @Override public int getMinHeight() { return 0; } // As per DimensionType, absolute
    @Override public int getMaxHeight() { return 384; } // As per DimensionType, absolute
    @Override public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos pos) {}
}
