package io.github.daxigua2333.mocai_clues.data.location;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.ModAttachmentRegistry;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.data.location.factory.FromClueBookSerializable;
import io.github.daxigua2333.mocai_clues.data.location.factory.ISerializableLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FromClueBook extends BaseRuntimeLocation {
    @Nullable
    private final Entity entity;

    public FromClueBook(Entity entity) {
        this.entity = entity;
    }

    public FromClueBook(ServerLevel level, UUID uuid) {
        this(level.getEntity(uuid));
    }

//    @Override
//    public Type type() {
//        return Type.ENTITY;
//    }

    // TODO: null behavior

    @Override
    public ObjectHolder<ClueObject> getHolder() {
        return entity.getData(ModAttachmentRegistry.CLUE_BOOK);
    }

    @Override
    public void markDirty() {
//        chunk.setUnsaved(true);
        entity.syncData(ModAttachmentRegistry.CLUE_BOOK);  // TODO: lazy sync
    }

    @Override
    public ISerializableLocation getSerializable() {
        return new FromClueBookSerializable(entity.getUUID());
    }

}
