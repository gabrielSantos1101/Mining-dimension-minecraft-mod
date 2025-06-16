package com.example.mod.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class MiningDimension {
    public static final ResourceKey<LevelStem> MINING_DIM_KEY = ResourceKey.create(Registries.LEVEL_STEM, new ResourceLocation("miningdimension", "mining_dim"));
    public static final ResourceKey<DimensionType> MINING_DIM_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE, new ResourceLocation("miningdimension", "mining_dim_type"));
}
