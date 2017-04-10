package org.nibiru.j2x.ast;

import static com.google.common.base.Preconditions.checkNotNull;

public class J2xArray extends J2xClass {
    private final J2xClass itemClass;

    public J2xArray(J2xClass itemClass) {
        super(itemClass.getName() + "[]",
                JAVA_LANG_PACKAGE,
                J2xClass.OBJECT,
                J2xAccess.PUBLIC);
        this.itemClass = checkNotNull(itemClass);
    }

    public J2xClass getItemClass() {
        return itemClass;
    }
}
