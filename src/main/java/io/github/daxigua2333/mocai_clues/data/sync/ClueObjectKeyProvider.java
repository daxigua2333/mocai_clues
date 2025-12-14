package io.github.daxigua2333.mocai_clues.data.sync;

import io.github.daxigua2333.mocai_clues.component.ClueObject;

public class ClueObjectKeyProvider implements MyObjectKeyProvider{

    @Override
    public String keyOf(ClueObject obj) {
        return obj.getId().toString();
    }
}
