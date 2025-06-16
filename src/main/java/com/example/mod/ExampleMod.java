package com.example.mod;

import com.example.mod.command.ModCommands;
import com.example.mod.dimension.DimensionInit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "miningdimension";

    public ExampleMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(DimensionInit::registerChunkGeneratorCodec);

        // Register command registration event listener on the NeoForge event bus
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    // Method to handle command registration
    public void onRegisterCommands(final RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}
