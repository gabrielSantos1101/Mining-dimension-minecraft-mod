package com.example.mod.world.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.example.mod.ExampleMod; 

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes; 
import net.minecraft.world.level.biome.Climate;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects; 
import java.util.Optional;

public class LayeredBiomeSource extends BiomeSource {

    private HolderGetter<Biome> biomeHolderGetter; 
    private List<Holder<Biome>> biomeLayers; 
    private final int layerHeight; 
    private Holder<Biome> defaultBiome; 
    // Store the original keys for rehydration via withStructureFeatures
    private final List<ResourceKey<Biome>> configuredBiomeKeys; 
    private final ResourceKey<Biome> configuredDefaultKey; 

    public static final List<ResourceKey<Biome>> DEFAULT_LAYER_BIOMES_KEYS = List.of(
        Biomes.PLAINS, Biomes.DESERT, Biomes.FOREST, Biomes.TAIGA, Biomes.SWAMP, 
        Biomes.SNOWY_PLAINS, Biomes.JUNGLE, Biomes.SAVANNA
    );

    public static final Codec<LayeredBiomeSource> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.list(ResourceKey.codec(Registries.BIOME)).fieldOf("biomes").orElse(DEFAULT_LAYER_BIOMES_KEYS).forGetter(src -> src.configuredBiomeKeys),
            Codec.INT.fieldOf("layer_height").orElse(20).forGetter(src -> src.layerHeight),
            ResourceKey.codec(Registries.BIOME).fieldOf("default_biome").orElse(Biomes.PLAINS).forGetter(src -> src.configuredDefaultKey)
        ).apply(instance, LayeredBiomeSource::new) // Uses the private constructor for codec pathway
    );
    
    public static void register() {
        Registry.register(BuiltInRegistries.BIOME_SOURCE, new ResourceLocation(ExampleMod.MODID, "layered_biome_source"), CODEC);
    }
    
    // Private constructor for Codec pathway - biomeHolderGetter is not available here
    private LayeredBiomeSource(List<ResourceKey<Biome>> biomeKeys, int layerHeight, ResourceKey<Biome> defaultBiomeKey) {
        super(new ArrayList<>()); // Super expects a list of possible biomes, but we don't have HolderGetter yet.
                                 // This will be populated by withStructureFeatures.
        this.configuredBiomeKeys = new ArrayList<>(biomeKeys); // Defensive copy
        this.layerHeight = layerHeight;
        this.configuredDefaultKey = defaultBiomeKey;
        // biomeHolderGetter, biomeLayers, defaultBiome will be initialized by withStructureFeatures
    }

    // Public constructor for programmatic instantiation where HolderGetter is available
    public LayeredBiomeSource(HolderGetter<Biome> biomeHolderGetter, List<ResourceKey<Biome>> biomeKeys, int layerHeight, ResourceKey<Biome> defaultBiomeKey) {
        super(resolveBiomeList(biomeHolderGetter, biomeKeys, defaultBiomeKey)); // Populate super's possibleBiomes
        this.biomeHolderGetter = biomeHolderGetter;
        this.configuredBiomeKeys = new ArrayList<>(biomeKeys); // Defensive copy
        this.layerHeight = layerHeight;
        this.configuredDefaultKey = defaultBiomeKey;
        // Initialize actual biome lists using the provided HolderGetter
        this.biomeLayers = resolveBiomeList(biomeHolderGetter, this.configuredBiomeKeys, this.configuredDefaultKey);
        this.defaultBiome = biomeHolderGetter.getOrThrow(this.configuredDefaultKey);
        
        // Ensure super.possibleBiomes is correctly set if resolveBiomeList returned empty and then added default
        if (this.possibleBiomes().isEmpty() && this.defaultBiome != null) {
            this.possibleBiomes = List.of(this.defaultBiome);
        }
    }
    
    private static List<Holder<Biome>> resolveBiomeList(HolderGetter<Biome> getter, List<ResourceKey<Biome>> keys, ResourceKey<Biome> defaultKey) {
        if (getter == null) return new ArrayList<>(); // Should not happen if called from the public constructor
        List<Holder<Biome>> resolvedBiomes = keys.stream()
                   .map(getter::get) // Returns Optional<Holder<Biome>>
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .collect(Collectors.toList());
        // If all configured biomes failed to resolve, ensure the default biome is in the list
        if(resolvedBiomes.isEmpty()){ 
            // Try to get the default biome, if it also fails, the list remains empty (which is problematic but indicates missing default)
            getter.get(defaultKey).ifPresent(resolvedBiomes::add);
        }
        return resolvedBiomes;
    }

    @Override
    public BiomeSource withStructureFeatures(HolderGetter<Biome> structureFeaturesHolderGetter) {
        // Create a new instance using the private constructor (or a dedicated one for this purpose)
        // then initialize its runtime fields with the provided HolderGetter.
        LayeredBiomeSource newSource = new LayeredBiomeSource(this.configuredBiomeKeys, this.layerHeight, this.configuredDefaultKey);
        
        newSource.biomeHolderGetter = structureFeaturesHolderGetter; 
        newSource.biomeLayers = resolveBiomeList(structureFeaturesHolderGetter, newSource.configuredBiomeKeys, newSource.configuredDefaultKey);
        newSource.defaultBiome = structureFeaturesHolderGetter.getOrThrow(newSource.configuredDefaultKey);
        
        // Populate the superclass's list of possible biomes
        Set<Holder<Biome>> possibleBiomesSet = new HashSet<>(newSource.biomeLayers);
        if(newSource.defaultBiome != null) possibleBiomesSet.add(newSource.defaultBiome);
        
        if (possibleBiomesSet.isEmpty()) { 
            // This is a fallback if ALL biome keys (including default) are invalid.
            System.err.println("LayeredBiomeSource: No biomes resolved, even default! Check configured biome keys. Using hardcoded PLAINS.");
            newSource.defaultBiome = structureFeaturesHolderGetter.getOrThrow(Biomes.PLAINS); 
            possibleBiomesSet.add(newSource.defaultBiome);
        }
        newSource.possibleBiomes = new ArrayList<>(possibleBiomesSet); // Set for BiomeSource superclass

        return newSource;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        // This method might be called before withStructureFeatures if not careful, 
        // or if the biome source is constructed and used without full worldgen setup.
        if (this.biomeLayers == null || this.biomeLayers.isEmpty() || this.defaultBiome == null) {
            // Attempt to self-initialize if HolderGetter is present (e.g. from public constructor)
            // but lists are somehow not populated.
            if (this.biomeHolderGetter != null) {
                 System.err.println("LayeredBiomeSource: biomeLayers or defaultBiome was null/empty in getNoiseBiome despite having a HolderGetter. Re-initializing.");
                this.biomeLayers = resolveBiomeList(this.biomeHolderGetter, this.configuredBiomeKeys, this.configuredDefaultKey);
                this.defaultBiome = this.biomeHolderGetter.getOrThrow(this.configuredDefaultKey);
                if (this.biomeLayers.isEmpty() && this.defaultBiome == null) { // Total failure
                     System.err.println("LayeredBiomeSource: Critical - Failed to resolve any biomes. Using hardcoded PLAINS.");
                     return BuiltInRegistries.BIOME.getHolderOrThrow(Biomes.PLAINS);
                }
            } else {
                // biomeHolderGetter is null, means codec pathway and withStructureFeatures hasn't run.
                // This is a problematic state. Fallback to hardcoded PLAINS.
                System.err.println("LayeredBiomeSource: biomeHolderGetter is null in getNoiseBiome (likely pre-initialization or incorrect usage). Using hardcoded PLAINS.");
                return BuiltInRegistries.BIOME.getHolderOrThrow(Biomes.PLAINS); 
            }
        }

        int blockY = quartY * 4; // Convert quartY to absolute Y for layer calculation
        // Calculate layer index based on blockY, assuming layers start from Y=0 of the dimension.
        // Dimension's min_y is handled by the ChunkGenerator.
        int layerIndex = Math.floorDiv(Math.max(0, blockY), layerHeight); 
        
        if (layerIndex >= 0 && layerIndex < biomeLayers.size()) {
            return biomeLayers.get(layerIndex);
        }
        // If Y is below 0 or above defined layers, return default.
        return Objects.requireNonNullElseGet(this.defaultBiome, () -> this.biomeHolderGetter.getOrThrow(Biomes.PLAINS)); 
    }
    
    public int getLayerHeight() {
        return this.layerHeight;
    }

    public int getLayerCount() {
        // Ensure biomeLayers is initialized before accessing size
        return this.biomeLayers != null ? this.biomeLayers.size() : 0;
    }
    
    public List<Holder<Biome>> getBiomesWithinYRange(int worldMinY, int worldMaxY) {
        Set<Holder<Biome>> biomesInRange = new HashSet<>();
        
        // Ensure necessary fields are initialized
        Holder<Biome> currentDefaultBiome = Objects.requireNonNullElseGet(this.defaultBiome, 
            () -> {
                if (this.biomeHolderGetter != null) return this.biomeHolderGetter.getOrThrow(this.configuredDefaultKey);
                System.err.println("LayeredBiomeSource: biomeHolderGetter is null in getBiomesWithinYRange. Using hardcoded PLAINS for default.");
                return BuiltInRegistries.BIOME.getHolderOrThrow(Biomes.PLAINS);
            }
        );

        if (this.biomeLayers == null || this.biomeLayers.isEmpty()) {
            System.err.println("LayeredBiomeSource: biomeLayers is null or empty in getBiomesWithinYRange. Returning only default biome.");
            biomesInRange.add(currentDefaultBiome);
            return new ArrayList<>(biomesInRange);
        }

        // Y coordinates are absolute block coordinates.
        int minLayerIndex = Math.floorDiv(Math.max(0, worldMinY), layerHeight); 
        int maxLayerIndex = Math.floorDiv(Math.max(0, worldMaxY), layerHeight);

        for (int i = minLayerIndex; i <= maxLayerIndex; i++) {
            if (i >= 0 && i < biomeLayers.size()) { // Ensure index is valid
                biomesInRange.add(biomeLayers.get(i));
            }
        }
        // If the range is outside all defined layers, or if no layers matched,
        // it implies the default biome would be prevalent in those areas.
        if (biomesInRange.isEmpty()) { 
            biomesInRange.add(currentDefaultBiome);
        }
        return new ArrayList<>(biomesInRange);
    }

    public int getLayerIndexForBiome(Holder<Biome> biomeHolder) {
        if (!biomeHolder.isBound() || this.biomeLayers == null || this.biomeLayers.isEmpty()) return -1;
        
        // Compare by ResourceKey for stability
        ResourceKey<Biome> targetKey = biomeHolder.unwrapKey().orElse(null);
        if (targetKey == null) return -1; // Cannot compare if the target has no key

        for (int i = 0; i < this.biomeLayers.size(); i++) {
            // Ensure the biome in the list is also bound and has a key
            if (this.biomeLayers.get(i).isBound()) {
                if (this.biomeLayers.get(i).unwrapKey().map(key -> key.equals(targetKey)).orElse(false)) {
                    return i;
                }
            }
        }
        return -1; // Biome not found in the defined layers
    }
}
