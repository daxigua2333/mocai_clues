package io.github.daxigua2333.mocai_clues.guis.widget;

import io.github.daxigua2333.mocai_clues.component.gui.FlexibleContainer;
import io.github.daxigua2333.mocai_clues.networks.C2SOpenItemPickerMenuPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public final class ItemStackPickerWidget extends FlexibleContainer {
    //    private ItemStack value;
    private final Supplier<ItemStack> valueSupplier;

    private final ItemStackSlotWidget slotWidget;
    private final Button button;


    public ItemStackPickerWidget(int x, int y,
//                                 int width, int height,
                                 Component component,
//                                 Screen parentScreen, ItemStack initial, Consumer<ItemStack> onChanged
                                 Supplier<ItemStack> valueSupplier) {
        super(x, y, 82, 22, component);
//        this.value = initial.copy();
        this.valueSupplier = valueSupplier;
        this.slotWidget = new ItemStackSlotWidget(x, y, valueSupplier);
        this.button = Button.builder(Component.literal("Pick..."), b -> {
            PacketDistributor.sendToServer(new C2SOpenItemPickerMenuPayload(getValue()));
//            Minecraft.getInstance().setScreen(
//            Minecraft.getInstance().pushGuiLayer(
//                    new InventoryItemPickerScreen(parentScreen, value, picked -> {
//                        this.value = picked;
//                        onChanged.accept(picked); // <- your callback
//                    })
//            );
        }).pos(x + 22, y - 1).size(60, 20).build();
    }


    public ItemStack getValue() {
        return valueSupplier.get();
    }


    @Override
    protected void processDirty() {

    }

    @Override
    protected void reLayout() {
        int x = this.getX();
        int y = this.getY();
        slotWidget.setPosition(x, y);
        button.setPosition(x + 22, y - 1);
    }

    @Override
    protected void renderTick(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        slotWidget.render(graphics, mouseX, mouseY, partialTick);
        button.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(slotWidget, button);
    }
}
