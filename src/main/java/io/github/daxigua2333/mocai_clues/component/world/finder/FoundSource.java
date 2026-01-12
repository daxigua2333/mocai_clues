package io.github.daxigua2333.mocai_clues.component.world.finder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.gui.uneditable.ScaledTextRow;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.html.parser.Entity;
import java.util.List;

public class FoundSource extends ClueComponent {
    private Component source;

    private FoundSource(Component source) {
        this.source = source;
    }
    public FoundSource() {
        this(Component.empty());
    }

    public Component getSource() {
        return source;
    }

    public void setSource(BlockPos pos) {
        source = Component.translatable("mocai_clue.finder.source.pos")
                .append(Component.literal(String.format("(%s, %s, %s)", pos.getX(), pos.getY(), pos.getZ())));
    }

    public void setSource(Entity entity) {
        source = Component.translatable("mocai_clue.finder.source.entity", entity.getName());  // TODO: test the name
    }

    public void setSource(Player player) {
        source = Component.translatable("mocai_clue.finder.source.player", player.getScoreboardName());
    }

    @Override
    public ComponentType type() {
        return ComponentType.FOUND_SOURCE;
    }

    public static final Codec<FoundSource> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ComponentSerialization.CODEC.fieldOf("source").forGetter(FoundSource::getSource)
    ).apply(inst, FoundSource::new));

    @Nullable
    @Override
    public List<AbstractWidget> getEditable() {
        return List.of();
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        return List.of(
                new ScaledTextRow(0, 0, 0, 20, 2, 2, source, 1)
        );
    }
}
