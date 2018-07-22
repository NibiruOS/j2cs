package org.nibiru.j2x.ast.element;

import javax.annotation.Nullable;

public class J2xLiteral {
    @Nullable
    private final Object value;

    public J2xLiteral(@Nullable Object value) {
        this.value = value;
    }

    @Nullable
    public Object getValue() {
        return value;
    }
}
