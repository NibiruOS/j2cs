package org.nibiru.j2x.ast.element;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class J2xAssignment {
    private final J2xVariable target;
    private final Object value;

    public J2xAssignment(J2xVariable target,
                         Object value) {
        this.target = checkNotNull(target);
        this.value = checkNotNull(value);
    }

    public J2xVariable getTarget() {
        return target;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        J2xAssignment that = (J2xAssignment) o;
        return Objects.equal(target, that.target) &&
                Objects.equal(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(target, value);
    }
}
