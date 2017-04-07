package org.nibiru.j2x.ast;

public class J2xConstructor extends J2xMethod {
    public J2xConstructor(String name,
                          J2xClass type,
                          J2xAccess access,
                          boolean isStatic,
                          boolean isFinal,
                          Iterable<J2xVariable> arguments,
                          J2xBlock body) {
        super(name, type, access, isStatic, isFinal, arguments, body);
    }
}
