package io.github.daxigua2333.mocai_clues.data;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.location.IRuntimeLocation;

import java.util.List;

public record ObjectsWithLocation(IRuntimeLocation location, List<ClueObject> objects) {

    public void addAll(ObjectsWithLocation other) {
        if (!location.equals(other.location)) {
            throw new RuntimeException("Invalid RetrieveResult merge.");
        }
        objects.addAll(other.objects);
    }
}
