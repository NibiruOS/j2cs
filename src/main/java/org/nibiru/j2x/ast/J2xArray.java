package org.nibiru.j2x.ast;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.checkNotNull;

public class J2xArray extends J2xClass {
    public static String ARRAY = "[]";
    private final J2xClass itemClass;
    private final int dimensions;

    public J2xArray(J2xClass itemClass,
                    int dimensions,
                    J2xClass objectClass) {
        super(itemClass.getName() + Strings.repeat(ARRAY, dimensions),
                JAVA_LANG_PACKAGE,
                objectClass,
                J2xAccess.PUBLIC);
        this.itemClass = checkNotNull(itemClass);
        this.dimensions = dimensions;
    }

    public J2xClass getItemClass() {
        return itemClass;
    }

    public int getDimensions() {
        return dimensions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        J2xArray array = (J2xArray) o;
        return dimensions == array.dimensions &&
                Objects.equal(itemClass, array.itemClass);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), itemClass, dimensions);
    }
}
