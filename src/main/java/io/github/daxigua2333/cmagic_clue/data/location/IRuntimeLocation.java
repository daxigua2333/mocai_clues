package io.github.daxigua2333.cmagic_clue.data.location;

import io.github.daxigua2333.cmagic_clue.component.ClueObject;
import io.github.daxigua2333.cmagic_clue.data.ObjectHolder;
import io.github.daxigua2333.cmagic_clue.data.location.factory.ISerializableLocation;

public interface IRuntimeLocation {

    ObjectHolder<ClueObject> getHolder();

    void markDirty();

    default void markDirty(ClueObject obj) {
        getHolder().markDirty(obj);
        markDirty();
    }

    ISerializableLocation getSerializable();
}
