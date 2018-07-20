package org.nibiru.j2x.ast.element;


import org.nibiru.j2x.ast.J2xClass;

import static com.google.common.base.Preconditions.checkNotNull;

public class J2xVariable extends J2xElement {
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
}
