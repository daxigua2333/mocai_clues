package io.github.daxigua2333.mocai_clues.data.sync;

import io.github.daxigua2333.mocai_clues.component.ClueObject;

@FunctionalInterface
public interface MyObjectKeyProvider {
    String keyOf(ClueObject obj);
}