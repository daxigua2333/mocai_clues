package io.github.daxigua2333.mocai_clues.data.location;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.data.location.factory.ISerializableLocation;

public interface IRuntimeLocation {

    ObjectHolder<ClueObject> getHolder();

    void markDirty();

    default void markDirty(ClueObject obj) {
        getHolder().markDirty(obj);
        markDirty();
    }

    ISerializableLocation getSerializable();
}
