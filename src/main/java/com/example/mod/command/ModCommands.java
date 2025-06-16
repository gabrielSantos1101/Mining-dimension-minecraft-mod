package com.example.mod.command;

import com.example.mod.dimension.MiningDimension; // For MINING_DIM_KEY
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands; // Correct import
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component; // For sending messages
import net.minecraft.core.BlockPos;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("miningdimensiontp")
                .requires(source -> source.hasPermission(2)) // OP permission level
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ServerLevel currentWorld = player.serverLevel(); // serverLevel() is the method
                    ServerLevel destinationWorld = currentWorld.getServer().getLevel(MiningDimension.MINING_DIM_KEY);

                    if (destinationWorld == null) {
                        context.getSource().sendFailure(Component.literal("Mining dimension (miningdimension:mining_dim) not found or not loaded!"));
                        return 0; // Failure
                    }

                    // Assuming grass layer is at Y=245, based on MiningChunkGenerator settings.
                    // Teleport to 0.5, 246, 0.5 for center of the block just above grass.
                    BlockPos teleportPosition = new BlockPos(0, 246, 0);

                    player.teleportTo(destinationWorld,
                                      teleportPosition.getX() + 0.5,
                                      teleportPosition.getY(),
                                      teleportPosition.getZ() + 0.5,
                                      player.getYRot(),
                                      player.getXRot());

                    context.getSource().sendSuccess(() -> Component.literal("Teleported to the mining dimension."), true);
                    return 1; // Success
                })
        );
    }
}
