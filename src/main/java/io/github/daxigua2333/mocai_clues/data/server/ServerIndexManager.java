package io.github.daxigua2333.mocai_clues.data.server;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;

public final class ServerIndexManager {
    private static void commonEnsure(ObjectHolder<ClueObject> holder) {

    }

    // TODO: 2 unsafe type declare... but if there is nothing other than ClueObject, it is safe
    @SuppressWarnings("unchecked")
    public static <T> void attachmentHolderEnsure(ObjectHolder<T> tHolder) {
        ObjectHolder<ClueObject> holder = (ObjectHolder<ClueObject>) tHolder;
        commonEnsure(holder);

    }

    @SuppressWarnings("unchecked")
    public static <T> void savedDataEnsure(ObjectHolder<T> tHolder) {
        ObjectHolder<ClueObject> holder = (ObjectHolder<ClueObject>) tHolder;
        commonEnsure(holder);

    }

}
