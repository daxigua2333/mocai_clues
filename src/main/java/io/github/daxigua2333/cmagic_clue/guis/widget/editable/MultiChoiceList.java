package io.github.daxigua2333.cmagic_clue.guis.widget.editable;

import io.github.daxigua2333.cmagic_clue.component.ComponentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

// TODO: refacrtor this shit
public class MultiChoiceList extends ObjectSelectionList<MultiChoiceList.Entry> {
    private final Set<ComponentType> selectedValues = new HashSet<>();
    private final Consumer<ComponentType> onSelected;
    private final Consumer<ComponentType> onCancelled;

    public MultiChoiceList(int width, int height, int top, int itemHeight,
                           List<ComponentType> options, Consumer<ComponentType> onSelected, Consumer<ComponentType> onCancelled) {
        super(Minecraft.getInstance(), width, height, top, itemHeight);
        this.onSelected = onSelected;
        this.onCancelled = onCancelled;
        // hard coded options
        for (var compo : options) {
//            this.addEntry(new Entry(compo, Component.translatable(compo.toString())));  // TODO: translate
            this.addEntry(new Entry(compo, Component.literal("11")));  // TODO: translate
        }
        this.height = itemHeight * options.size();
    }

//    public void addOption(String id, Component label) {
//        this.addEntry(new Entry(id, label));
//    }
//
//    public Set<String> getSelectedValues() {
//        return selectedValues;
//    }

    @Override
    public int getRowWidth() {
//        Make rows slightly narrower than the full width so the scrollbar has space
        return this.width - 10;
    }
    @Override
    protected int getScrollbarPosition() {
        // Right edge of the list; SCROLLBAR_WIDTH is handled internally.
        return this.getX() + this.getWidth() - 6;
    }
    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {}
    @Override
    protected void renderDecorations(GuiGraphics guiGraphics, int mouseX, int mouseY) {}


    public class Entry extends ObjectSelectionList.Entry<Entry> {
        private final ComponentType id;
        private final Component label;

        public Entry(ComponentType id, Component label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            boolean isSelected = selectedValues.contains(id);

            // Draw a background if selected (optional)
//            if (isSelected) {
//                guiGraphics.fill(left, top, left + width, top + height, 0x55FFFFFF);
//            }

            // Draw Checkbox indicator
            String prefix = isSelected ? "[X] " : "[ ] ";
            guiGraphics.drawString(Minecraft.getInstance().font, prefix + label.getString(), left + 5, top + (height / 2) - 4, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (selectedValues.contains(id)) {
                selectedValues.remove(id);
                onCancelled.accept(id);
            } else {
                selectedValues.add(id);
                onSelected.accept(id);
            }
            return true; // Return true to consume the click
        }

        @Override
        public Component getNarration() {
            return label;
        }
    }
}
