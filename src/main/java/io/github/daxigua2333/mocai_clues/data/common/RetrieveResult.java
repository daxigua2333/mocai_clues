package io.github.daxigua2333.mocai_clues.data.common;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.location.IRuntimeLocation;

import java.util.List;

public record RetrieveResult(IRuntimeLocation location, List<ClueObject> objects) {
}
