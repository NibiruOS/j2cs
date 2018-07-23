package org.nibiru.j2x.ast.element;

import com.google.common.base.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        J2xReturn j2xReturn = (J2xReturn) o;
        return Objects.equal(value, j2xReturn.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
