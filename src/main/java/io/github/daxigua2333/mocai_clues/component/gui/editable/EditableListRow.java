//package io.github.daxigua2333.mocai_clues.component.gui.editable;
//
//import com.mojang.blaze3d.vertex.Tesselator;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Font;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.components.AbstractContainerWidget;
//import net.minecraft.client.gui.components.AbstractWidget;
//import net.minecraft.client.gui.components.Button;
//import net.minecraft.client.gui.components.events.GuiEventListener;
//import net.minecraft.client.gui.narration.NarrationElementOutput;
//import net.minecraft.network.chat.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Supplier;
//
//public class EditableListRow extends AbstractContainerWidget {
//
//    private List<AbstractWidget> children = new ArrayList<>();
//    private final int spacing;  // spacing between each widgets
//    private final Supplier<AbstractWidget> createDefault;
//
//    private Button create;
//    private List<Button> delete;
//
//    private float lastPartialTick;
//
//    public EditableListRow(int x, int y, int width, List<AbstractWidget> children, Supplier<AbstractWidget> createDefault) {
//        super(x, y, width, 0, Component.empty());  // height will be calculated and hot updated in ticker
//
//        Font font = Minecraft.getInstance().font;
//        this.children = children;
//        this.spacing = 2;
//        this.createDefault = createDefault;
//
//        this.create = Button.builder(Component.literal("+"), btn -> {
//            children.add(createDefault.get());
//            delete.add();
//    //        this.onChange();
//        }).bounds(0, 0, width - 10 - 2, 10).build();
//        for (int i=0; i<children.size(); i++) {
//            var child = children.get(i);
//            delete.add(Button.builder(Component.literal("-"), btn -> {
//                children.remove(finalI);
////                this.onChange();
//            }).bounds(x + width - deleteW, y, deleteW, deleteH).build());
//
//        }
//    }
//
//    /** hot update*/
//    public void updateChildren(List<AbstractWidget> children) {
//        this.children = children;
//    }
//
//    @Override
//    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
//        // Optional: draw a background for the panel
//        // gfx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x80000000);
//
//        // Render all children
//        int x = getX();
//        int y = getY();
//
//        for (int i=0; i<children.size(); i++) {
//            // position child
//            var child = children.get(i);
//            child.setX(x);
//            child.setY(y);
//
//            int deleteW = 10;
//            int deleteH = 10;
//            child.setWidth(this.width - deleteW - 2);
//
//            // make delete button
//            int finalI = i;
//            Button delete = Button.builder(Component.literal("-"), btn -> {
//                children.remove(finalI);
////                this.onChange();
//            }).bounds(x + width - deleteW, y, deleteW, deleteH).build();
//
//            // draw child
//            child.render(gfx, mouseX, mouseY, partialTick);
//            delete.render(gfx, mouseX, mouseY, partialTick);
//
//            y += child.getHeight() + spacing;
//        }
//
//        // draw create button
//        Button create = Button.builder(Component.literal("+"), btn -> {
//            children.add(createDefault.get());
////            this.onChange();
//        }).bounds(x, y, width - 10 - 2, 10).build();
//        create.render(gfx, mouseX, mouseY, partialTick);
//        y += create.getHeight() + spacing;
//
//        // adjust widget height
//        this.height = y - getY();
//    }
//
//    @Override
//    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
//
//    }
//
//    @Override
//    public List<? extends GuiEventListener> children() {
//        return children;
//    }
//
//}
