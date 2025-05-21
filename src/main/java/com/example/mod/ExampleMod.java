package com.example.mod;

import com.example.mod.init.ModBlocks;
import com.example.mod.init.ModDimensions;
import com.example.mod.init.ModItems;
import com.example.mod.world.biome.LayeredBiomeSource; // Import
import com.example.mod.world.gen.LayeredBiomeChunkGenerator; // Import
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent; // Import this
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ExampleMod.MODID) // Ensure MODID is used here
public class ExampleMod {
    public static final String MODID = "examplemod"; // Make sure this matches mods.toml

    public ExampleMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModDimensions.register(modEventBus); // For DimensionTypes

        // Register the setup method for FMLCommonSetupEvent
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        LayeredBiomeChunkGenerator.register();
        LayeredBiomeSource.register(); // Add this line
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // This is a good place for things that need to run after registries are set up
            // but before worlds load, or for thread-safe operations.
            ModDimensions.assignDimensionTypes(); // Assign our DimensionType object
        });
    }
}
