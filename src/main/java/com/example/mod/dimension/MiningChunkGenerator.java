package com.example.mod.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelSource;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.List;
import java.util.Optional;

public class MiningChunkGenerator extends FlatLevelSource {

    public static final Codec<MiningChunkGenerator> CODEC = RecordCodecBuilder.create((instance) ->
        instance.group(
            RegistryOps.retrieveGetter(Registries.BIOME)
        ).apply(instance, MiningChunkGenerator::new)
    );

    public MiningChunkGenerator(Registry<Biome> biomeRegistry) {
        super(createSettings(biomeRegistry));
    }

    private static FlatLevelGeneratorSettings createSettings(Registry<Biome> biomeRegistry) {
        Holder<Biome> biomeHolder = biomeRegistry.getHolderOrThrow(Biomes.PLAINS);

        return new FlatLevelGeneratorSettings(
                Optional.empty(),
                biomeHolder,
                List.of(
                        new FlatLayerInfo(1, Blocks.BEDROCK),       // Y=0
                        new FlatLayerInfo(59, Blocks.END_STONE),    // Y=1 to Y=59
                        new FlatLayerInfo(60, Blocks.NETHERRACK),   // Y=60 to Y=119
                        new FlatLayerInfo(120, Blocks.STONE),       // Y=120 to Y=239
                        new FlatLayerInfo(5, Blocks.DIRT),          // Y=240 to Y=244
                        new FlatLayerInfo(1, Blocks.GRASS_BLOCK)    // Y=245
                )
        );
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}
