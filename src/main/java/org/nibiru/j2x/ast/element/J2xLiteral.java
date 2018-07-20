package org.nibiru.j2x.ast.element;

public class J2xLiteral {
    private final Object value;

    public J2xLiteral(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
