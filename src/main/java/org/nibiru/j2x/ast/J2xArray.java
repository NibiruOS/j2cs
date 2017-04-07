package org.nibiru.j2x.ast;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class J2xArray extends J2xClass {
    private final J2xClass itemClass;

    public J2xArray(String name,
                    String packageName,
                    @Nullable J2xClass superClass,
                    J2xAccess access,
                    J2xClass itemClass) {
        super(name, packageName, superClass, access);
        this.itemClass = checkNotNull(itemClass);
    }

    public J2xClass getItemClass() {
        return itemClass;
    }
}
