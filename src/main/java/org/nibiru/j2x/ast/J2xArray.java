package org.nibiru.j2x.ast;

import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.checkNotNull;

public class J2xArray extends J2xClass {
    public static String ARRAY = "[]";
    private final J2xClass itemClass;
    private final int dimensions;

    public J2xArray(J2xClass itemClass, int dimensions) {
        super(itemClass.getName() + Strings.repeat(ARRAY, dimensions),
                JAVA_LANG_PACKAGE,
                J2xClass.OBJECT,
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
}
