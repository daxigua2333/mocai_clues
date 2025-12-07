package io.github.daxigua2333.mocai_clues.component;

public abstract class ClueComponent {
    public abstract ComponentType type();

    protected ClueObject owner;
    public void setOwner(ClueObject owner) {
        this.owner = owner;
    }

    public ClueComponent(){}

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
