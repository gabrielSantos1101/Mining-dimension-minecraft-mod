package com.example.mod.block;

import com.example.mod.world.ModDimensionTypes; // Import for dimension keys
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer; // For ServerPlayer
import net.minecraft.server.level.ServerLevel; // For ServerLevel
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
// Required for teleportation
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.ITeleporter; // NeoForge Teleporter interface
import java.util.function.Function;


public class MiningDimensionPortalBlock extends Block {

    public MiningDimensionPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, net.minecraft.world.phys.BlockHitResult hit) {
        if (!worldIn.isClientSide() && player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            ServerLevel currentLevel = serverPlayer.serverLevel();
            
            // Check if the DimensionType object is available
            if (ModDimensionTypes.MINING_DIMENSION_TYPE == null) {
                serverPlayer.sendSystemMessage(Component.literal("Mining dimension type not initialized!"));
                System.err.println("ModDimensionTypes.MINING_DIMENSION_TYPE is null. Make sure it's assigned after registration.");
                return InteractionResult.FAIL;
            }

            if (currentLevel.dimension() != ModDimensionTypes.MINING_DIM_KEY) {
                ServerLevel destinationLevel = serverPlayer.getServer().getLevel(ModDimensionTypes.MINING_DIM_KEY);
                if (destinationLevel != null) {
                    serverPlayer.sendSystemMessage(Component.literal("Teleporting to Mining Dimension..."));
                    // Use a custom teleporter or a simple one for now
                    serverPlayer.changeDimension(destinationLevel, new ITeleporter() {
                        @Override
                        public PortalInfo getPortalInfo(ServerPlayer player, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                            // Simple teleporter: find a safe spot or default to world spawn.
                            // For a superflat world, y=64 (or higher, depending on biome layers later) should be safe.
                            BlockPos spawnPos = destWorld.getSharedSpawnPos(); // Or a fixed safe Y value for superflat
                            // A more robust solution would find the highest solid block.
                            return new PortalInfo(new Vec3(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5), player.getDeltaMovement(), player.getYRot(), player.getXRot());
                        }
                    });
                } else {
                    serverPlayer.sendSystemMessage(Component.literal("Mining Dimension not found/loaded!"));
                    return InteractionResult.FAIL;
                }
            } else {
                ServerLevel overworld = serverPlayer.getServer().getLevel(Level.OVERWORLD);
                if (overworld != null) {
                    serverPlayer.sendSystemMessage(Component.literal("Teleporting back to Overworld..."));
                    serverPlayer.changeDimension(overworld, new ITeleporter() {
                         @Override
                        public PortalInfo getPortalInfo(ServerPlayer player, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                            BlockPos spawnPos = destWorld.getSharedSpawnPos();
                            return new PortalInfo(new Vec3(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5), player.getDeltaMovement(), player.getYRot(), player.getXRot());
                        }
                    });
                } else {
                     serverPlayer.sendSystemMessage(Component.literal("Overworld not found/loaded!"));
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(worldIn.isClientSide());
    }
}
