package org.nibiru.j2x.asm;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.LinkedList;

class Stack {
    private final LinkedList<Object> stack;

    Stack() {
        stack = Lists.newLinkedList();
    }

    public void push(Object element) {
        stack.push(element);
    }

    public <T> T pop() {
        return (T) stack.pop();
    }

    public Collection<Object> asCollection() {
        // TODO: totalmente ineficiente, podrian mantenerse 2 listas donde se pushee/popee al reves
        return Lists.reverse(stack);
    }
}
