package com.example.mod.dimension;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.ChunkGenerator;
import net.neoforged.neoforge.registries.RegisterEvent;

public class DimensionInit {

    public static void registerChunkGeneratorCodec(final RegisterEvent event) {
        // Check if the event is for registering ChunkGenerators
        if (event.getRegistryKey().equals(Registries.CHUNK_GENERATOR)) {
            // Register our custom chunk generator's codec
            event.register(Registries.CHUNK_GENERATOR,
                new ResourceLocation("miningdimension", "mining_chunk_generator"),
                MiningChunkGenerator.CODEC);
        }
    }

    // The following methods are designed for Data Generation using a BootstrapContext.
    // They are not directly called in this subtask but illustrate the registration process.

    public static void bootstrapDimensionType(BootstrapContext<DimensionType> context) {
        // Registers the custom dimension type
        context.register(MiningDimension.MINING_DIM_TYPE_KEY, MiningDimensionType.create());
    }

    public static void bootstrapLevelStem(BootstrapContext<LevelStem> context) {
        // Registers the custom dimension (LevelStem)
        // This method would typically retrieve registered DimensionType and ChunkGenerator settings
        // (often as Holders from the BootstrapContext) and combine them into a LevelStem.
        // For example:
        // Holder<DimensionType> dimensionTypeHolder = context.lookup(Registries.DIMENSION_TYPE).getOrThrow(MiningDimension.MINING_DIM_TYPE_KEY);
        // Holder<ChunkGenerator> chunkGeneratorHolder = ... ; // Get holder for the configured chunk generator
        // context.register(MiningDimension.MINING_DIM_KEY, new LevelStem(dimensionTypeHolder, chunkGeneratorHolder.value()));
        // The actual instantiation of MiningChunkGenerator or its settings would be part of the DataGen process.
    }
}
