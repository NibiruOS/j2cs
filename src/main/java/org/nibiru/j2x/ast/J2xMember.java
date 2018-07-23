package org.nibiru.j2x.ast;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class J2xMember {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        J2xMember j2xMember = (J2xMember) o;
        return isStatic == j2xMember.isStatic &&
                isFinal == j2xMember.isFinal &&
                Objects.equal(name, j2xMember.name) &&
                Objects.equal(type, j2xMember.type) &&
                access == j2xMember.access;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, type, access, isStatic, isFinal);
    }
}
