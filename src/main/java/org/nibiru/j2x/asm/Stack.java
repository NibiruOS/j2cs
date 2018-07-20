package org.nibiru.j2x.asm;

import com.google.common.collect.Lists;

import java.util.Deque;

class Stack {
    private final Deque<Object> stack;

    Stack() {
        stack = Lists.newLinkedList();
    }

    public void push(Object element) {
        stack.push(element);
    }

    public <T> T pop() {
        return (T) stack.pop();
    }

}
