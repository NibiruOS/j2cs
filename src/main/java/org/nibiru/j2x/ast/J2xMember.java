package org.nibiru.j2x.ast;

import static com.google.common.base.Preconditions.checkNotNull;

public class J2xMember {

    private final String name;
    private final J2xClass type;
    private final J2xAccess access;
    private final boolean isStatic;
    private final boolean isFinal;

    public J2xMember(String name,
                     J2xClass type,
                     J2xAccess access,
                     boolean isStatic,
                     boolean isFinal) {
        this.name = checkNotNull(name);
        this.type = checkNotNull(type);
        this.access = checkNotNull(access);
        this.isStatic = isStatic;
        this.isFinal = isFinal;
    }

    public String getName() {
        return name;
    }

    public J2xClass getType() {
        return type;
    }

    public J2xAccess getAccess() {
        return access;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isFinal() {
        return isFinal;
    }
}
