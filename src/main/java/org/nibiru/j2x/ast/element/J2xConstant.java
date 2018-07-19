package org.nibiru.j2x.ast.element;

public class J2xConstant extends J2xElement {
    private final Object value;

    public J2xConstant(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
