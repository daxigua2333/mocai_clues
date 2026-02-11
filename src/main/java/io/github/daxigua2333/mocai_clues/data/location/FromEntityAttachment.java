package io.github.daxigua2333.mocai_clues.data.location;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.ModAttachmentRegistry;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.data.location.factory.FromEntityAttachmentSerializable;
import io.github.daxigua2333.mocai_clues.data.location.factory.ISerializableLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FromEntityAttachment extends BaseRuntimeLocation {
    @Nullable
    private final Entity entity;

    public FromEntityAttachment(Entity entity) {
        this.entity = entity;
    }

    public FromEntityAttachment(ServerLevel level, UUID uuid) {
        this(level.getEntity(uuid));
    }

//    @Override
//    public Type type() {
//        return Type.ENTITY;
//    }

    // TODO: null behavior

    @Override
    public ObjectHolder<ClueObject> getHolder() {
        return entity.getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);
    }

    @Override
    public void markDirty() {
//        chunk.setUnsaved(true);
        entity.syncData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);  // TODO: lazy sync
    }

    @Override
    public ISerializableLocation getSerializable() {
        return new FromEntityAttachmentSerializable(entity.getUUID());
    }


}
