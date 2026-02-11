package io.github.daxigua2333.mocai_clues.data.location;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.data.client.ClientObjectHolderInSavedData;
import io.github.daxigua2333.mocai_clues.data.location.factory.FromSavedDataSerializable;
import io.github.daxigua2333.mocai_clues.data.location.factory.ISerializableLocation;
import io.github.daxigua2333.mocai_clues.data.server.ClueObjectHolderInSavedData;
import net.minecraft.world.level.Level;

public class FromSavedData extends BaseRuntimeLocation {
    private final Level level;

    public FromSavedData(Level level) {
        this.level = level;
    }

    @Override
    public ObjectHolder<ClueObject> getHolder() {
        if (level.isClientSide()) {
            return ClientObjectHolderInSavedData.getInstance().getHolder();
        } else {
            return ClueObjectHolderInSavedData.getInstance(level.getServer()).holder();
        }
    }

    @Override
    public void markDirty() {
        if (!level.isClientSide()) {
            ClueObjectHolderInSavedData.getInstance(level.getServer()).setDirty();
        }
    }

    @Override
    public ISerializableLocation getSerializable() {
        return new FromSavedDataSerializable();
    }
}
