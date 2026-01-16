package io.github.daxigua2333.mocai_clues.component.world.finder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.Config;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlashDotSet extends ClueComponent {
//    private Map<BlockPos, Set<Direction>>
    private record BlockPosFace(BlockPos pos, Direction face) {
        public static final Codec<BlockPosFace> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(BlockPosFace::pos),
                Direction.CODEC.fieldOf("face").forGetter(BlockPosFace::face)
        ).apply(inst, BlockPosFace::new));
    }

    private Set<BlockPosFace> backing = new HashSet<>();

    private FlashDotSet(Set<BlockPosFace> backing) {
        this.backing = new HashSet<>(backing);
    }
    public FlashDotSet() {
        this(new HashSet<>());
    }

    private Set<BlockPosFace> getBacking() {
        return backing;
    }

    public void add(BlockPos pos, Direction face) {
        backing.add(new BlockPosFace(pos, face));
    }

    private static void spawnOne(ClientLevel level, BlockPos pos, Direction face) {
        final double EPS = 0.55; // slightly outside the block surface
        double x = pos.getX() + 0.5 + face.getStepX() * EPS;
        double y = pos.getY() + 0.5 + face.getStepY() * EPS;
        double z = pos.getZ() + 0.5 + face.getStepZ() * EPS;
        float r = Config.CLIENT.FLASH_DOT_R.getAsInt() / 255f;
        float g = Config.CLIENT.FLASH_DOT_G.getAsInt() / 255f;
        float b = Config.CLIENT.FLASH_DOT_B.getAsInt() / 255f;
//        var dust = new DustParticleOptions(new Vector3f(1.0f, 0.9f, 0.2f), 1f);
        var dust = new DustParticleOptions(new Vector3f(r, g, b), 1f);
        // "true" = alwaysRender (ignores “Minimal” particles setting)
        level.addParticle(dust, true, x, y, z, 0.0, 0.0, 0.0);
    }

    public void spawn(ClientLevel level) {
        FinderState sCompo = owner.getComponent(ComponentType.FINDER_STATE);
        if (sCompo == null) throw new RuntimeException("ClueObject#" + this.owner.getId() + " has no FinderState component");
        if (!sCompo.isDoRenderFlashDot()) {
            return;
        }
        if (!sCompo.isAccessible(Minecraft.getInstance().player.getScoreboardName())) {
            return;
        }

        for (BlockPosFace entry : backing) {
            spawnOne(level, entry.pos(), entry.face());
        }
    }

    @Override
    public ComponentType type() {
        return ComponentType.FLASH_DOT_SET;
    }

    public static final Codec<FlashDotSet> CODEC =
            NeoForgeExtraCodecs.setOf(BlockPosFace.CODEC)
                    .xmap(FlashDotSet::new, FlashDotSet::getBacking);

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
