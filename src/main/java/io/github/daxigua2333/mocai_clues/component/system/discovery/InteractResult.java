package io.github.daxigua2333.mocai_clues.component.system.discovery;

import io.github.daxigua2333.mocai_clues.component.Assembler;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.finder.SendClue;
import io.github.daxigua2333.mocai_clues.data.ModAttachmentRegistry;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.data.ObjectHolderLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public final class InteractResult {
    // TODO: dirty.  merge, send message callback
    public static void sendClue(ServerPlayer player, ClueObject obj, ObjectHolderLocation location) {
        SendClue compo = obj.getComponent(ComponentType.SEND_CLUE);
        if (compo == null) return;

        ClueObject copy;
        if (location.data() instanceof ObjectHolderLocation.BlockPosWithFace data) {
            // chunk
            copy = Assembler.createClueBookClue(obj, data.pos());
        } else if (location.data() instanceof BlockPos pos) {
            copy = Assembler.createClueBookClue(obj, pos);
        } else if (location.data() instanceof UUID data) {
            // entity
            Entity e = ((ServerLevel) player.level()).getEntity(data);
            copy = Assembler.createClueBookClue(obj, e);
        } else {
            throw new RuntimeException("Invalid ObjectHolderLocation.");
        }

        player.sendSystemMessage(Component.translatable("You have found a new clue!"));
        ObjectHolder<ClueObject> holder = player.getData(ModAttachmentRegistry.CLUE_BOOK);
        holder.put(copy);
        player.syncData(ModAttachmentRegistry.CLUE_BOOK);

    }

}
