//package io.github.daxigua2333.mocai_clues.component.world.finder;
//
//import com.mojang.serialization.Codec;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import io.github.daxigua2333.mocai_clues.component.ClueComponent;
//import io.github.daxigua2333.mocai_clues.component.ComponentType;
//import net.minecraft.client.gui.components.AbstractWidget;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.world.item.ItemStack;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ItemClue extends ClueComponent {
//    private BlockPos pos;
//    private Direction face;
//    //    private final ItemStack[] stacks;
//    private final List<ItemStack> stacks;
////    private static final int CAPACITY = 4;
//
//    private ItemClue(List<ItemStack> stacks, Direction face, BlockPos pos) {
//        this.stacks = new ArrayList<>(stacks);
//        this.face = face;
//        this.pos = pos;
//    }
//
//    public ItemClue() {
//        this(List.of(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY), null, null);
//    }
//
//    public BlockPos getPos() {
//        return pos;
//    }
//
//    public Direction getFace() {
//        return face;
//    }
//
//    public List<ItemStack> getStacks() {
//        return List.copyOf(stacks);
//    }
//
//    public void setPos(BlockPos pos) {
//        this.pos = pos;
//    }
//
//    public void setFace(Direction face) {
//        this.face = face;
//    }
//
//    public void setStack(int i, ItemStack stack) {
//        if (i >= 4 || i < 0) throw new RuntimeException("ItemClue component exceeds max capacity.");
//        stacks.set(i, stack);
//    }
//
//    @Override
//    public ComponentType type() {
//        return ComponentType.ITEM_CLUE;
//    }
//
//    public static final Codec<ItemClue> CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("stacks").forGetter(ItemClue::getStacks),
//            Direction.CODEC.fieldOf("face").forGetter(ItemClue::getFace),
//            BlockPos.CODEC.fieldOf("pos").forGetter(ItemClue::getPos)
//    ).apply(inst, ItemClue::new));
//
//    @Nullable
//    @Override
//    public List<AbstractWidget> getEditable() {
//        return List.of();
//    }
//
//    @Nullable
//    @Override
//    public List<AbstractWidget> getUneditable() {
//        return List.of();
//    }
//}
