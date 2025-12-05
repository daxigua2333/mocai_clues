package io.github.daxigua2333.mocai_clues.component.network;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.data.server.ClueObjectMainMapInSavedData;
import net.minecraft.server.level.ServerLevel;

public class ManualClueServerHandler extends BaseSyncHandler {

    public ManualClueServerHandler() {
        super();
    }

    public void handleSync(ServerLevel level) {
        ClueObject object = this.owner;
        ClueObjectMainMapInSavedData.getInstance(level).put(object.getId(), object);
    }

    @Override
    public ComponentType type() {
        return ComponentType.MANUAL_CLUE_SERVER_HANDLER;
    }

    public static final Codec<ManualClueServerHandler> CODEC = Codec.unit(new ManualClueServerHandler());
}
