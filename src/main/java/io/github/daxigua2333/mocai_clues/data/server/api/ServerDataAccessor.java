package io.github.daxigua2333.mocai_clues.data.server.api;

import io.github.daxigua2333.mocai_clues.component.Assembler;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosSet;
import io.github.daxigua2333.mocai_clues.data.client.ClientObjectHolderInSavedData;
import io.github.daxigua2333.mocai_clues.data.server.ClueObjectHolderInSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// TODO: route to SD or something...
public final class ServerDataAccessor {

    // ======== retrieve =========
    // TODO: mutable and mark dirty issues......
    public static ClueObject retrieveByIDInSD(MinecraftServer server, UUID id) {
        return ClueObjectHolderInSavedData.getInstance(server).holder().get(id);
    }
    public static List<ClueObject> retrieveByBlockPos(Level level, BlockPos pos) {
        List<ClueObject> result = new ArrayList<>();

        var server = level.getServer();
        if (server == null) return new ArrayList<>();
        var holder = ClueObjectHolderInSavedData.getInstance(server).holder();

        for (var obj : holder.values()) {
            BlockPosSet compo = obj.getComponent(ComponentType.BLOCK_POS_SET);
            if (compo == null) continue;
            for (var cPos : compo.getImmutable()) {
                if (pos.equals(cPos)) {
                    result.add(obj);
                    break;
                }
            }
        }

        return result;
    }
    public static List<ClueObject> retrieveByEntity(Entity entity) {
        return List.of();
    }


    // ========== upsert ==========

    public static void createDefault(Level level) {
        var obj = Assembler.createManualClue();
        var holder = ClueObjectHolderInSavedData.getInstance(level.getServer());
        holder.put(obj);
    }

    public static void upsertInSD(MinecraftServer server, ClueObject obj) {
        ClueObjectHolderInSavedData.getInstance(server).put(obj);
    }

    public static void deleteInSD(MinecraftServer server, UUID id) {
        ClueObjectHolderInSavedData.getInstance(server).remove(id);
    }
}
