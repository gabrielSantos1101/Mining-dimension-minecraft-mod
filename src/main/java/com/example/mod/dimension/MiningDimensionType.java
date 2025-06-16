package com.example.mod.dimension;

import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantIntValue;
import net.minecraft.world.level.dimension.DimensionEffects;
import net.minecraft.world.level.dimension.DimensionType;
import java.util.OptionalLong;

public class MiningDimensionType {

    public static DimensionType create() {
        return new DimensionType(
                OptionalLong.empty(), // fixedTime
                true,  // hasSkylight
                false, // hasCeiling
                false, // ultrawarm
                true,  // natural
                1.0,   // coordinateScale
                false, // bedWorks
                false, // respawnAnchorWorks
                0,     // minY
                256,   // height (multiple of 16, accommodating 0-245)
                246,   // logicalHeight (actual top Y is 245)
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn tag for blocks like netherrack
                DimensionEffects.OVERWORLD_EFFECTS, // visual effects
                0.0f, // ambientLight
                new DimensionType.MonsterSettings(false, true, ConstantIntValue.of(0), 0) // Standard mob spawning
        );
    }
}
