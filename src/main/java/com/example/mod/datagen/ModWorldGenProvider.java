package com.example.mod.datagen;

import com.example.mod.ExampleMod;
import com.example.mod.world.ModDimensionTypes;
import com.example.mod.world.gen.LayeredBiomeChunkGenerator;
import com.example.mod.world.biome.LayeredBiomeSource; // Import new biome source

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup; // Required for CompletableFuture<HolderLookup.Provider>
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey; // Required for ResourceKey
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes; // For default biome key
import net.minecraft.world.level.biome.BiomeSource; // For BiomeSource general type
// Removed FixedBiomeSource import as it's replaced
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
// Removed NeoForgeRegistries import if not directly used for biome lookup here

import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletableFuture; // Required for CompletableFuture

public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {

    private static DimensionType createMiningDimensionType() {
        return new DimensionType(
                OptionalLong.empty(), // fixedTime
                true,  // hasSkylight
                false, // hasCeiling (set to false as per typical overworld-like dimensions)
                false, // ultraWarm
                true,  // natural
                1.0,   // coordinateScale
                true,  // bedWorks
                false, // respawnAnchorWorks
                0,     // minY
                384,   // height 
                384,   // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                new ResourceLocation("overworld"), // effectsLocation
                0.0f,  // ambientLight
                new DimensionType.MonsterSettings(false, false, UniformInt.of(0,0), 0) // monsterSettings
        );
    }
    
    // Updated to use LayeredBiomeSource
    // The BootstapContext for LevelStem can lookup DimensionType, but for BiomeSource we need the HolderLookup.Provider
    private static LevelStem createMiningLevelStem(BootstapContext<LevelStem> stemContext, HolderLookup.Provider lookupProvider) {
        // Obtain HolderGetter for Biomes from the lookupProvider
        HolderGetter<Biome> biomeHolderGetter = lookupProvider.lookupOrThrow(Registries.BIOME);

        BiomeSource layeredBiomeSourceInstance = new LayeredBiomeSource(
            biomeHolderGetter, // Pass the HolderGetter<Biome>
            LayeredBiomeSource.LAYER_BIOMES_KEYS, // List of ResourceKey<Biome>
            20, // Layer Height
            Biomes.PLAINS // Default biome ResourceKey
        );
        
        // Obtain Holder for DimensionType from the LevelStem's BootstapContext
        Holder<DimensionType> dimTypeHolder = stemContext.lookup(Registries.DIMENSION_TYPE).getOrThrow(ModDimensionTypes.MINING_DIM_TYPE_KEY);

        return new LevelStem(
            dimTypeHolder,
            new LayeredBiomeChunkGenerator(layeredBiomeSourceInstance)
        );
    }

    private static void bootstrapDimensionTypes(BootstapContext<DimensionType> typeContext) {
        typeContext.register(ModDimensionTypes.MINING_DIM_TYPE_KEY, createMiningDimensionType());
    }
    
    // Updated bootstrapLevelStems to correctly pass the HolderLookup.Provider and the right context type
    private static void bootstrapLevelStems(BootstapContext<LevelStem> stemContext) {
        // The HolderLookup.Provider is available from the BootstapContext itself.
        HolderLookup.Provider lookupProvider = stemContext.lookupProvider();
        stemContext.register(ModDimensionTypes.MINING_DIM_KEY, createMiningLevelStem(stemContext, lookupProvider));
    }

    // Update the BUILDER to correctly pass the context to bootstrapLevelStems
    // The context itself contains the lookupProvider.
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
        .add(Registries.DIMENSION_TYPE, ModWorldGenProvider::bootstrapDimensionTypes)
        .add(Registries.LEVEL_STEM, ModWorldGenProvider::bootstrapLevelStems);


    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(ExampleMod.MODID));
    }
}
