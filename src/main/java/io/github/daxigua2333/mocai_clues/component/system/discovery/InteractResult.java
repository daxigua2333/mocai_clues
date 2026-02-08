package io.github.daxigua2333.mocai_clues.component.system.discovery;

import io.github.daxigua2333.mocai_clues.component.Assembler;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.finder.FoundSource;
import io.github.daxigua2333.mocai_clues.component.world.finder.SendClue;
import io.github.daxigua2333.mocai_clues.data.ModAttachmentRegistry;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.data.ObjectHolderLocation;
import io.github.daxigua2333.mocai_clues.data.location.IRuntimeLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.UUID;

public final class InteractResult {
    public static void sendClue(ServerPlayer player, ClueObject obj, ObjectHolderLocation location) {
        SendClue compo = obj.getComponent(ComponentType.SEND_CLUE);
        if (compo == null) return;

        // generate the copy in clue book
        ClueObject copy;
        if (location.data() instanceof ObjectHolderLocation.BlockPosWithFace data) {  // chunk
            copy = Assembler.createClueBookClue(obj, data.pos());
        } else if (location.data() instanceof BlockPos pos) {
            copy = Assembler.createClueBookClue(obj, pos);
        } else if (location.data() instanceof UUID data) {  // entity
            Entity e = ((ServerLevel) player.level()).getEntity(data);
            copy = Assembler.createClueBookClue(obj, e);
        } else if (location.data() instanceof Player from) {  // player share
            FoundSource fCompo = obj.getComponent(ComponentType.FOUND_SOURCE);
            if (fCompo == null) throw new RuntimeException("Invalid shared obj: no FoundSource component.");
            fCompo.setSource(from);
            copy = obj;
        } else {
            throw new RuntimeException("Invalid ObjectHolderLocation.");
        }

        ObjectHolder<ClueObject> holder = player.getData(ModAttachmentRegistry.CLUE_BOOK);
        // TODO: merge logic, attention to dirty things
        if (!holder.containsKey(copy.getId())) {  // new
            holder.put(copy);
            player.sendSystemMessage(Component.translatable("mocai_clues.finder.result.new"));
        } else {
            if (copy.equals(holder.get(copy.getId()))) {  // repeat
                player.sendSystemMessage(Component.translatable("mocai_clues.finder.result.repeated"));
            } else {  // merge
                holder.put(copy);
                player.sendSystemMessage(Component.translatable("mocai_clues.finder.result.update"));
            }
        }
        // setUnsaved
        player.syncData(ModAttachmentRegistry.CLUE_BOOK);

    }

    public static void sendItem() {

    }

}
