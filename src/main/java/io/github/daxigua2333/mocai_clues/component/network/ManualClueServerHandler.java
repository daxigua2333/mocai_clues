package io.github.daxigua2333.mocai_clues.component.network;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.data.server.ClueObjectHolderInSavedData;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;

public class ManualClueServerHandler extends BaseSyncHandler {

    public ManualClueServerHandler() {
        super();
    }

    public void handleSync(ServerLevel level) {
        ClueObject object = this.owner;
        ClueObjectHolderInSavedData.getInstance(level.getServer()).put(object);  // TODO: use database api
    }

    @Override
    public ComponentType type() {
        return ComponentType.MANUAL_CLUE_SERVER_HANDLER;
    }

    public static final Codec<ManualClueServerHandler> CODEC = Codec.unit(new ManualClueServerHandler());

    @Nullable
    @Override
    public LinkedHashMap<String, AbstractWidget> getEditable() {
        return null;
    }

    @Nullable
    @Override
    public LinkedHashMap<String, AbstractWidget> getUneditable() {
        return null;
    }

}
