package org.nibiru.j2x.ast;

public class J2xField extends J2xMember {
    public J2xField(String name,
                    J2xClass type,
                    J2xAccess access,
                    boolean isStatic,
                    boolean isFinal) {
        super(name, type, access, isStatic, isFinal);
    }
}
