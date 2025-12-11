package io.github.daxigua2333.mocai_clues.component.gui.editable;


import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StringListWidget extends AbstractContainerWidget {

    private final Font font;
    private final List<Row> rows = new ArrayList<>();
    private final List<AbstractWidget> children = new ArrayList<>();
    private final Button addButton;

    private int rowHeight = 20;
    private int rowSpacing = 4;
    private int maxLength = 256;

    private Consumer<List<String>> changeListener;

    public StringListWidget(Font font,
                            int x, int y,
                            int width, int height,
                            List<String> initialValues) {
        super(x, y, width, height, Component.empty());
        this.font = font;

        // Add button (we re-position it in relayout()).
        this.addButton = Button.builder(Component.literal("+"),
            btn -> addRow("")
        ).bounds(x, y, 40, rowHeight).build();
        this.children.add(this.addButton);

        if (initialValues == null || initialValues.isEmpty()) {
            addRow("");
        } else {
            for (String value : initialValues) {
                addRow(value);
            }
        }

        relayout();
    }

    /** Optional: called whenever rows are added/removed or text changes. */
    public void setChangeListener(Consumer<List<String>> changeListener) {
        this.changeListener = changeListener;
    }

    /** Current list contents. */
    public List<String> getValues() {
        List<String> values = new ArrayList<>(rows.size());
        for (Row row : rows) {
            values.add(row.editBox.getValue());
        }
        return values;
    }

    /** Replace all rows with the given values. */
    public void setValues(List<String> newValues) {
        for (Row row : rows) {
            children.remove(row.editBox);
            children.remove(row.deleteButton);
        }
        rows.clear();

        if (newValues == null || newValues.isEmpty()) {
            addRow("");
        } else {
            for (String value : newValues) {
                addRow(value);
            }
        }

        relayout();
        onChanged();
    }

    /** Optional per-field character limit. */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        for (Row row : rows) {
            row.editBox.setMaxLength(maxLength);
        }
    }

    // ---------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------

    private void onChanged() {
        if (this.changeListener != null) {
            this.changeListener.accept(getValues());
        }
    }

    private void addRow(String initialValue) {
        EditBox editBox = new EditBox(
            this.font,
            this.getX(), this.getY(),
            100, rowHeight,
            Component.empty()
        );
        editBox.setMaxLength(this.maxLength);
        editBox.setValue(initialValue);
        // Notify when the text changes.
        editBox.setResponder(s -> onChanged());

        Row row = new Row(editBox);

        Button deleteButton = Button.builder(Component.literal("X"),
            btn -> removeRow(row)
        ).bounds(this.getX(), this.getY(), 20, rowHeight).build();

        row.deleteButton = deleteButton;

        this.rows.add(row);
        this.children.add(editBox);
        this.children.add(deleteButton);

        relayout();
        onChanged();
    }

    private void removeRow(Row row) {
        if (!this.rows.contains(row)) {
            return;
        }

        this.rows.remove(row);
        this.children.remove(row.editBox);
        this.children.remove(row.deleteButton);

        // Optionally enforce at least one row:
        if (this.rows.isEmpty()) {
            addRow("");
        } else {
            relayout();
            onChanged();
        }
    }

    private void relayout() {
        int x = this.getX();
        int y = this.getY();

        int totalWidth = this.getWidth();
        int deleteWidth = 20;
        int spacing = 4;
        int editWidth = Math.max(40, totalWidth - deleteWidth - spacing);

        int currentY = y;

        for (Row row : rows) {
            row.editBox.setX(x);
            row.editBox.setY(currentY);
            row.editBox.setWidth(editWidth);
            row.editBox.setHeight(rowHeight);

            row.deleteButton.setX(x + editWidth + spacing);
            row.deleteButton.setY(currentY);
            row.deleteButton.setWidth(deleteWidth);
            row.deleteButton.setHeight(rowHeight);

            currentY += rowHeight + rowSpacing;
        }

        // Place the add button below the rows (clamp to widget height).
        int addY = currentY;
//        int maxY = y + this.getHeight() - rowHeight;
//        if (addY > maxY) {
//            addY = maxY;
//        }

        this.addButton.setX(x);
        this.addButton.setY(addY);
        this.addButton.setWidth(60);
        this.addButton.setHeight(rowHeight);

        this.height = addY + rowHeight + rowSpacing - y;

        // Sync visibility/active flags with parent.
        boolean visible = this.visible;
        boolean active = this.active;
        for (AbstractWidget child : children) {
            child.visible = visible;
            child.active = active;
        }
    }

    // If the widget is moved or resized, keep children in sync.
    @Override
    public void setX(int x) {
        super.setX(x);
        relayout();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        relayout();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        relayout();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        relayout();
    }

    // ---------------------------------------------------------------------
    // Widget lifecycle / events
    // ---------------------------------------------------------------------

//    @Override
//    public void tick() {
//        // Screen#tick will call this; we just tick the EditBoxes.
//        for (Row row : rows) {
//            row.editBox.tick();
//        }
//    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Background or border could be drawn here if you want.
        // For now, we just render children.
        for (AbstractWidget child : children) {
            if (child.visible) {
                child.render(graphics, mouseX, mouseY, partialTick);
            }
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

//    @Override
//    public boolean mouseClicked(double mouseX, double mouseY, int button) {
//        for (AbstractWidget child : children) {
//            if (child.mouseClicked(mouseX, mouseY, button)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public boolean mouseReleased(double mouseX, double mouseY, int button) {
//        for (AbstractWidget child : children) {
//            if (child.mouseReleased(mouseX, mouseY, button)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
//        for (AbstractWidget child : children) {
//            if (child.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
//        for (AbstractWidget child : children) {
//            if (child.mouseScrolled(mouseX, mouseY, delta)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//        for (AbstractWidget child : children) {
//            if (child.keyPressed(keyCode, scanCode, modifiers)) {
//                return true;
//            }
//        }
//        return false;
//    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (Row row : rows) {
            if (row.editBox.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        // You could aggregate narration from children here if you care about accessibility.
    }

    private static final class Row {
        final EditBox editBox;
        Button deleteButton;

        Row(EditBox editBox) {
            this.editBox = editBox;
        }
    }
}
