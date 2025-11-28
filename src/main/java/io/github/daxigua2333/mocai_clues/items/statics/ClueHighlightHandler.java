package io.github.daxigua2333.mocai_clues.items.statics;

import io.github.daxigua2333.mocai_clues.Config;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = MoCaiClues.MODID)
public class ClueHighlightHandler {  // TODO: performance issues
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return; // only server
        if (!shouldShowHighlights(player)) return;
        // get block positions from chunk data you already attach
        ServerLevel serverLevel = (ServerLevel) player.level();
        List<BlockPos> highlights = ClueContainerSearchUtils.findAttachmentsInRadius(serverLevel, player.position(), Config.COMMON.WAND_HIGHLIGHT_RADIUS.get());
        for (BlockPos pos : highlights) {
            highlightBlockOutline(serverLevel, (ServerPlayer) player, pos);
        }
    }
    private static boolean shouldShowHighlights(Player p) {
        // TODO: otherwise maybe checking certain armor, effect...
        return p.getMainHandItem().getItem() == ModItemsRegistry.CLUE_WAND_ITEM.get();
    }

    // highlight particles part
    // TODO: high performance implement
    public static void highlightBlockOutline(
            ServerLevel world,
            ServerPlayer player,
            BlockPos pos,
            int stepsPerEdge,
            ParticleOptions particle,
            boolean longDistance
    ) {
        if (world == null || player == null || pos == null) return;
        if (stepsPerEdge <= 0) stepsPerEdge = 8;

        double x0 = pos.getX();
        double y0 = pos.getY();
        double z0 = pos.getZ();
        double x1 = x0 + 1.0;
        double y1 = y0 + 1.0;
        double z1 = z0 + 1.0;

        // Define the 12 edges as pairs of endpoints
        double[][][] edges = new double[][][] {
            {{x0,y0,z0},{x1,y0,z0}},
            {{x0,y1,z0},{x1,y1,z0}},
            {{x0,y0,z1},{x1,y0,z1}},
            {{x0,y1,z1},{x1,y1,z1}},
            {{x0,y0,z0},{x0,y1,z0}},
            {{x1,y0,z0},{x1,y1,z0}},
            {{x0,y0,z1},{x0,y1,z1}},
            {{x1,y0,z1},{x1,y1,z1}},
            {{x0,y0,z0},{x0,y0,z1}},
            {{x1,y0,z0},{x1,y0,z1}},
            {{x0,y1,z0},{x0,y1,z1}},
            {{x1,y1,z0},{x1,y1,z1}}
        };

        // For each edge, sample points and spawn a single particle at each sample
        for (double[][] edge : edges) {
            double ax = edge[0][0], ay = edge[0][1], az = edge[0][2];
            double bx = edge[1][0], by = edge[1][1], bz = edge[1][2];

            for (int i = 0; i <= stepsPerEdge; i++) {
                double t = i / (double) stepsPerEdge; // careful fractional arithmetic
                double px = lerp(ax, bx, t);
                double py = lerp(ay, by, t);
                double pz = lerp(az, bz, t);

                // Send a single particle targeted at `player`.
                // count = 1, offsets = 0, speed = 0 => should place a single particle at (px,py,pz).
                world.sendParticles(player, particle, longDistance, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    // Convenience overload with sane defaults
    public static void highlightBlockOutline(ServerLevel world, ServerPlayer player, BlockPos pos) {
        highlightBlockOutline(world, player, pos, 10, ParticleTypes.END_ROD, false);  // TODO: config
    }
}
