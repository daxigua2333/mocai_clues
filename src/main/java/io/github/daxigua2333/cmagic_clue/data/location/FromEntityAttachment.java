package io.github.daxigua2333.cmagic_clue.data.location;

import io.github.daxigua2333.cmagic_clue.component.ClueObject;
import io.github.daxigua2333.cmagic_clue.data.ModAttachmentRegistry;
import io.github.daxigua2333.cmagic_clue.data.ObjectHolder;
import io.github.daxigua2333.cmagic_clue.data.location.factory.FromEntityAttachmentSerializable;
import io.github.daxigua2333.cmagic_clue.data.location.factory.ISerializableLocation;
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

    // TODO: null behavior, but entity death will auto delete the Attachment, so whatever

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
