package com.example.mod.world;

import com.example.mod.ExampleMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class ModDimensionTypes {
    public static final ResourceKey<LevelStem> MINING_DIM_KEY = ResourceKey.create(
        Registries.LEVEL_STEM, new ResourceLocation(ExampleMod.MODID, "mining_dim")
    );

    public static final ResourceKey<DimensionType> MINING_DIM_TYPE_KEY = ResourceKey.create(
        Registries.DIMENSION_TYPE, new ResourceLocation(ExampleMod.MODID, "mining_dim_type")
    );

    // This field will hold the registered DimensionType object.
    // Registration will happen in ModDimensions.java
    public static DimensionType MINING_DIMENSION_TYPE; 
}
