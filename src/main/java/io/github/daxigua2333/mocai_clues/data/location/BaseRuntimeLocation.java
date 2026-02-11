package io.github.daxigua2333.mocai_clues.data.location;

import java.util.Objects;

public abstract class BaseRuntimeLocation implements IRuntimeLocation {

    @Override
     public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        IRuntimeLocation that = (IRuntimeLocation) object;
        return Objects.equals(getSerializable(), that.getSerializable());

    }

}
