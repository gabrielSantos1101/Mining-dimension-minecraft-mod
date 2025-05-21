package com.example.mod.datagen;

import com.example.mod.ExampleMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        // ExistingFileHelper existingFileHelper = event.getExistingFileHelper(); // Not needed for WorldGen only
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Add the WorldGen provider (for dimensions, dimension_types, etc.)
        generator.addProvider(event.includeServer(), new ModWorldGenProvider(packOutput, lookupProvider));
        
        // Example for block states and item models (can be added later)
        // ModBlockStateProvider blockStateProvider = new ModBlockStateProvider(packOutput, existingFileHelper);
        // generator.addProvider(event.includeClient(), blockStateProvider);
        // generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper, blockStateProvider.getExistingFileHelper()));

        // Example for recipes (can be added later)
        // generator.addProvider(event.includeServer(), ModRecipeProvider.create(packOutput, lookupProvider));
    }
}
