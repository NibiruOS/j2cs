package org.nibiru.j2x.ast.element;

import com.google.common.base.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        J2xLiteral that = (J2xLiteral) o;
        return Objects.equal(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
