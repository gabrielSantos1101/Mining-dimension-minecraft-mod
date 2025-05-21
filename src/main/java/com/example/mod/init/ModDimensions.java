package com.example.mod.init;

import com.example.mod.ExampleMod;
import com.example.mod.world.ModDimensionTypes; // Import the new class
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext; // Corrected typo from BootstapContext to BootstrapContext
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.resources.ResourceKey; 
import net.minecraft.resources.ResourceLocation; 
import net.minecraft.tags.BlockTags; 
import net.minecraft.util.valueproviders.UniformInt; 
import java.util.OptionalLong; 

// NeoForge specific event for dimension registration (if needed later for LevelStem)
// import net.neoforged.neoforge.common.world.RegisterDimensionsEvent;


import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegistryObject;


public class ModDimensions {
    public static final DeferredRegister<DimensionType> DIMENSION_TYPES =
        DeferredRegister.create(Registries.DIMENSION_TYPE, ExampleMod.MODID);

    // Registration of the DimensionType object
    public static final RegistryObject<DimensionType> MINING_DIM_TYPE_REG_OBJ = DIMENSION_TYPES.register(
        ModDimensionTypes.MINING_DIM_TYPE_KEY.location().getPath(), // "mining_dim_type"
        () -> new DimensionType(
                OptionalLong.empty(), // fixedTime
                true,  // hasSkylight
                false, // hasCeiling
                false, // ultraWarm
                true,  // natural
                1.0,   // coordinateScale
                true,  // bedWorks
                false, // respawnAnchorWorks
                0,     // minY
                256,   // height
                256,   // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                new ResourceLocation("overworld"), // effectsLocation (e.g., overworld, the_nether, the_end)
                0.0f, // ambientLight
                new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0) // monsterSettings
        )
    );
    
    public static void register(IEventBus eventBus) {
        DIMENSION_TYPES.register(eventBus);
    }

    // Method to be called from a high-priority event listener (e.g. DataGen or specific NeoForge event)
    // For now, we are only registering DimensionType via DeferredRegister.
    // LevelStem (which defines the ChunkGenerator) will be handled next, likely via datagen.
    public static void bootstrapDimension(BootstapContext<LevelStem> context) { // Corrected BootstapContext
        // This is where we would define and register the LevelStem with its ChunkGenerator.
        // Example (actual implementation will be more complex for custom biomes):
        // context.register(ModDimensionTypes.MINING_DIM_KEY, new LevelStem(
        //     ModDimensionTypes.MINING_DIM_TYPE_KEY, // This needs to be the ResourceKey for the registered DimensionType
        //     new FlatLevelSource(new FlatLevelGeneratorSettings(...)) // Example superflat generator
        // ));
        // For now, this method is a placeholder for where LevelStem registration would occur.
        // The actual ChunkGenerator for layered biomes will be defined in the next steps.
    }
     // Helper to assign the registered DimensionType to our static field once available.
    public static void assignDimensionTypes() {
        if (MINING_DIM_TYPE_REG_OBJ.isPresent()) {
            ModDimensionTypes.MINING_DIMENSION_TYPE = MINING_DIM_TYPE_REG_OBJ.get();
        } else {
            // Handle error or log - this means the DimensionType wasn't registered as expected.
            System.err.println("Mining Dimension Type was not registered correctly!");
        }
    }
}
