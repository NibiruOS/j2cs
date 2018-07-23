package org.nibiru.j2x.ast.element;


import com.google.common.base.Objects;

import org.nibiru.j2x.ast.J2xClass;

import static com.google.common.base.Preconditions.checkNotNull;

public class J2xVariable {
    private final static String THIS_NAME = "this";
    private String name;
    private J2xClass type;

    public J2xVariable() {
    }

    public J2xVariable(String name,
                       J2xClass type) {
        this.name = checkNotNull(name);
        this.type = checkNotNull(type);
    }

    public boolean isThis() {
        return THIS_NAME.equals(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(J2xClass type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public J2xClass getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        J2xVariable variable = (J2xVariable) o;
        return Objects.equal(name, variable.name) &&
                Objects.equal(type, variable.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, type);
    }
}
