package org.nibiru.j2x.asm;

import com.google.common.collect.Lists;

import org.nibiru.j2x.ast.element.J2xElement;

import java.util.Deque;

class Stack {
    private final Deque<J2xElement> stack;

    Stack() {
        stack = Lists.newLinkedList();
    }

    public void push(J2xElement element) {
        stack.push(element);
    }

    public <T extends J2xElement> T pop() {
        return (T) stack.pop();
    }

}
