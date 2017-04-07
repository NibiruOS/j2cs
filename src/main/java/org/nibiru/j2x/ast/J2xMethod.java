package org.nibiru.j2x.ast;

public class J2xMethod extends J2xMember {
    private final Iterable<J2xVariable> arguments;
    private final J2xBlock body;

    public J2xMethod(String name,
                     J2xClass type,
                     J2xAccess access,
                     boolean isStatic,
                     boolean isFinal,
                     Iterable<J2xVariable> arguments,
                     J2xBlock body) {
        super(name, type, access, isStatic, isFinal);
        this.arguments = arguments;
        this.body = body;
    }

    public Iterable<J2xVariable> getArguments() {
        return arguments;
    }
}
