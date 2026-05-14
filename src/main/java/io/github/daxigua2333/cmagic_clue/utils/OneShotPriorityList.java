package io.github.daxigua2333.cmagic_clue.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OneShotPriorityList<T> {

    private static class Node<T> {
        final int priority;
        final T value;

        Node(int priority, T value) {
            this.priority = priority;
            this.value = value;
        }
    }

    private final List<Node<T>> list = new ArrayList<>();
    private boolean sealed = false;

    public void add(int priority, T value) {
        if (sealed) {
            throw new IllegalStateException("Cannot add after seal/iteration started");
        }
        list.add(new Node<>(priority, value));
    }

    // Call this once when you've finished adding
    public void prepareForIteration() {
        if (!sealed) {
            // sort by priority descending
            list.sort(Comparator.comparingInt((Node<T> n) -> n.priority).reversed());
            sealed = true;
        }
    }

    public Iterable<T> valuesDescending() {
        prepareForIteration();
        List<T> result = new ArrayList<>(list.size());
        for (Node<T> node : list) {
            result.add(node.value);
        }
        return result;
    }

    // Optional: help GC by clearing references explicitly if you want
    public void clear() {
        list.clear();
        sealed = false;
    }
}
