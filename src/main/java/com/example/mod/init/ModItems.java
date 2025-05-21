package com.example.mod.init;

import com.example.mod.ExampleMod; // Adjust if MODID is not public static
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.bus.api.IEventBus;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, ExampleMod.MODID); // Assuming MODID is in ExampleMod

    // Example placeholder for an item (will be replaced/used later)
    // public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item",
    //        () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
