package com.example.mod.init;

import com.example.mod.ExampleMod;
import com.example.mod.block.MiningDimensionPortalBlock; // Added import
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material; // Added import
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegistryObject;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, ExampleMod.MODID);

    // Define properties for the portal block
    public static final BlockBehaviour.Properties MINING_PORTAL_PROPERTIES =
        BlockBehaviour.Properties.of(Material.STONE)
            .strength(50.0F, 1200.0F) // Similar to Obsidian
            .requiresCorrectToolForDrops();

    // Register the portal block
    public static final RegistryObject<Block> MINING_PORTAL_BLOCK = registerBlock("mining_dimension_portal_block",
            () -> new MiningDimensionPortalBlock(MINING_PORTAL_PROPERTIES));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, new Item.Properties()); // Pass new Item.Properties()
        return toReturn;
    }

    // Updated to accept Item.Properties for the BlockItem
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, Item.Properties properties) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), properties));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
