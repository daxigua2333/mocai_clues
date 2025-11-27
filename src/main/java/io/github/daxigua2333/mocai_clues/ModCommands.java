package io.github.daxigua2333.mocai_clues;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.daxigua2333.mocai_clues.footprints.ModFootprintRegistry;
import io.github.daxigua2333.mocai_clues.footprints.data.FootprintMainMap;
import io.github.daxigua2333.mocai_clues.footprints.statics.CreateEventHooks;
import io.github.daxigua2333.mocai_clues.footprints.statics.FootprintServerHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class ModCommands {


    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("footprint")
                    // who can use it? permission level 0 = everyone, 2 = operator
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.literal("create")
                            .then(Commands.argument("count", IntegerArgumentType.integer())
                                    .executes(ModCommands::createFootprints)
                            )
                    )
        );

        dispatcher.register(
                Commands.literal("footprint")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("delete")
                                .executes(ModCommands::deleteFootprints))
        );

    }

    private static int createFootprints(CommandContext<CommandSourceStack> ctx) {
        // read the <int> argument
        int count = IntegerArgumentType.getInteger(ctx, "count");

        // get the player that executed the command
        ServerPlayer player;
        try {
            player = ctx.getSource().getPlayerOrException(); // throws if run from console/command block
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            ctx.getSource().sendFailure(Component.literal("This command can only be run by a player."));  // TODO: lang
            return 0;
        }

        // --- your logic using player + value ---
        player.sendSystemMessage(
            Component.literal(count + " footprints created."));

        for(int i=0; i<count; i++) {
            Level level = player.level();
            AABB box = player.getBoundingBox();
            // Bottom center of the bounding box, nudged slightly up to avoid z-fighting
            BlockPos blockBelow = player.getOnPos();
            Vec3 feet = box.getBottomCenter();

            // === Horizontal rotation ===
            float yaw = player.getYRot();

            // === Size-based footprint long/short side ===
            double xSize = box.getXsize();
            double zSize = box.getZsize();
            double max = Math.max(xSize, zSize);
            double min = Math.min(xSize, zSize);
            // Scale however you like; these are just sane defaults.
            float longSide  = (float) (max * 0.6D); // along facing/move direction
            float shortSide = (float) (min * 0.6D); // across the foot

            double offset = (double) i / (double) count;
            FootprintServerHelper.create(level, blockBelow, feet.x+offset, feet.y , feet.z, yaw, longSide, shortSide, 1);
        }

        return 1;
    }

    private static int deleteFootprints(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player;
        try {
            player = ctx.getSource().getPlayerOrException(); // throws if run from console/command block
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            ctx.getSource().sendFailure(Component.literal("This command can only be run by a player."));  // TODO: lang
            return 0;
        }

        // --- your logic using player + value ---
        player.sendSystemMessage(
            Component.literal("footprints in current chunk deleted"));

        ServerLevel level = player.serverLevel();
        LevelChunk chunk = level.getChunkAt(player.getOnPos());
        FootprintServerHelper.deleteByChunk(chunk);

        return 1;

    }
}
