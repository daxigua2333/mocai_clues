package io.github.daxigua2333.mocai_clues.component;

import net.minecraft.client.gui.components.AbstractWidget;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public abstract class ClueComponent {
    public abstract ComponentType type();

    protected ClueObject owner;
    public void setOwner(ClueObject owner) {
        this.owner = owner;
    }

    public ClueComponent(){}


    // ======= gui part ========
    @OnlyIn(Dist.CLIENT)
    @Nullable
    public abstract List<AbstractWidget> getEditable(Runnable markDirty);
    @OnlyIn(Dist.CLIENT)
    @Nullable
    public abstract List<AbstractWidget> getUneditable();


//    void tick();
//    public abstract void onAdded(ClueObject owner);

    // ==== codec ====
//    public static final Codec<ClueComponent> CODEC =
//            ComponentType.CODEC.dispatch(
//                    ClueComponent::type,
//                    type -> switch (type) {
////                        case FOO -> Foo.CODEC;
//                        case META_DATA -> MetaData.CODEC;
//                    }
//            );

}
