package io.github.daxigua2333.mocai_clues.component.storage;


import io.github.daxigua2333.mocai_clues.component.ClueComponent;

/**
 * when need to create a index, iterate all subclasses of this
 * */
public abstract class BaseIndex extends ClueComponent {
    public abstract void createIndex();
}
