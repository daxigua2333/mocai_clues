package io.github.daxigua2333.cmagic_clue.data;

import io.github.daxigua2333.cmagic_clue.component.ClueObject;
import io.github.daxigua2333.cmagic_clue.data.location.IRuntimeLocation;

import java.util.List;

public record ObjectsWithLocation(IRuntimeLocation location, List<ClueObject> objects) {

    public void addAll(ObjectsWithLocation other) {
        if (!location.equals(other.location)) {
            throw new RuntimeException("Invalid RetrieveResult merge.");
        }
        objects.addAll(other.objects);
    }
}
