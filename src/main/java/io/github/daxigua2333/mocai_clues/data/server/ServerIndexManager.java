package io.github.daxigua2333.mocai_clues.data.server;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosSet;
import io.github.daxigua2333.mocai_clues.component.world.renderer.PassType;
import io.github.daxigua2333.mocai_clues.component.world.renderer.RendererHolder;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import net.minecraft.world.level.ChunkPos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
