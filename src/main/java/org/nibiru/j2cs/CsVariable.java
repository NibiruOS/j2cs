package org.nibiru.j2cs;

class CsVariable {
    private final String name;
    private final String type;

    CsVariable(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
