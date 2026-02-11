package io.github.daxigua2333.mocai_clues.data.common;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.location.IRuntimeLocation;

import java.util.List;

public record RetrieveResult(IRuntimeLocation location, List<ClueObject> objects) {

    public void addAll(RetrieveResult other) {
        if (!location.equals(other.location)) {
            throw new RuntimeException("Invalid RetrieveResult merge.");
        }
        objects.addAll(other.objects);
    }
}
