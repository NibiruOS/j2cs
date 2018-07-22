package org.nibiru.j2x.ast.element;

import static com.google.common.base.Preconditions.checkNotNull;

public class J2xReturn {
    private final Object value;

    public J2xReturn() {
        value = null;
    }

    public J2xReturn(Object value) {
        this.value = checkNotNull(value);
    }

    public Object getValue() {
        return value;
    }
}
