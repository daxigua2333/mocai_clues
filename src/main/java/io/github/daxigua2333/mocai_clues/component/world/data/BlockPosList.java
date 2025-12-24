package io.github.daxigua2333.mocai_clues.component.world.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** in chunk attachment, this exists only once
 *  but in SavedData, this can exist multiply */
public class BlockPosList extends ClueComponent {
    private final Set<BlockPos> set;

    public Set<BlockPos> getImmutable() {
        return Collections.unmodifiableSet(set);
    }
    private HashSet<BlockPos> getSet() {
        return new HashSet<>(set);
    }
    public void add(BlockPos pos) {
        set.add(pos);
    }
    public void clear() {
        set.clear();
    }

    public BlockPosList() {
        this(new HashSet<>());
    }
    private BlockPosList(Set<BlockPos> set) {
        this.set = set;
    }

    @Override
    public ComponentType type() {
        return ComponentType.BLOCK_POS_LIST;
    }

    public static final Codec<BlockPosList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().xmap(HashSet::new, List::copyOf).fieldOf("set").forGetter(BlockPosList::getSet)
    ).apply(instance, BlockPosList::new));

    @Nullable
    @Override
    public List<AbstractWidget> getEditable() {
        return List.of();
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        return List.of();
    }
}
