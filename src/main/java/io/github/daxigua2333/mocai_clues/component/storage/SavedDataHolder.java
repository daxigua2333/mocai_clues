package io.github.daxigua2333.mocai_clues.component.storage;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;

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
//        ClueObjectMainMapInSavedData.getInstance(level).put(obj.getId(), obj);
//        ClueObjectHolderInSavedData.getInstance(level.getServer()).put(obj);
    }

    @Override
    public ComponentType type() {
        return ComponentType.SAVED_DATA_HOLDER;
    }

    public static final Codec<SavedDataHolder> CODEC = Codec.unit(new SavedDataHolder());

    @Nullable
    @Override
    public List<AbstractWidget> getEditable() {
        return null;
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        return null;
    }
}
