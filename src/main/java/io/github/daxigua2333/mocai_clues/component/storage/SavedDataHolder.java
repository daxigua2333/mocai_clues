package io.github.daxigua2333.mocai_clues.component.storage;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.data.server.ClueObjectMainMapInSavedData;
import net.minecraft.server.level.ServerLevel;

public class SavedDataHolder extends BaseDataHolder{
    private final ServerLevel level;

    // ====== constructor ======
    public SavedDataHolder(ServerLevel level) {
        this.level = level;
    }
    private SavedDataHolder() {
        this(null);
    }

    // ====== behavior =======
    @Override
    public void onCreate() {
        var obj = this.owner;
        ClueObjectMainMapInSavedData.getInstance(level).put(obj.getId(), obj);
    }

    @Override
    public ComponentType type() {
        return ComponentType.SAVED_DATA_HOLDER;
    }

    public static final Codec<SavedDataHolder> CODEC = Codec.unit(new SavedDataHolder());
}
