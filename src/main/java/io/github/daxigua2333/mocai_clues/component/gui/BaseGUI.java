package io.github.daxigua2333.mocai_clues.component.gui;

import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.utils.OneShotPriorityList;
import net.minecraft.client.gui.components.AbstractWidget;

/**
 * #addWidget will build the widget it provides,
 * then add to the list with list.add(priority, widget),
 * finally in Screen, sort by priority, and iterate the list with addRenderableWidget()
 * */
public abstract class BaseGUI extends ClueComponent {
    private int priority;
    public abstract void addWidget(OneShotPriorityList<AbstractWidget> list);
}
