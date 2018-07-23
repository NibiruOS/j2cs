package org.nibiru.j2x.ast;

import org.nibiru.j2x.ast.element.J2xVariable;

import static com.google.common.base.Preconditions.checkNotNull;

public class J2xMethod extends J2xMember {
    public final static String CONSTRUCTOR_NAME = "<init>";
    public final static String STATIC_CONSTRUCTOR_NAME = "<clinit>";
    private final String argDesc;
    private final Iterable<J2xVariable> arguments;
    private final J2xBlock body;

    public J2xMethod(String name,
                     J2xClass type,
                     J2xAccess access,
                     boolean isStatic,
                     boolean isFinal,
                     String argDesc,
                     Iterable<J2xVariable> arguments,
                     J2xBlock body) {
        super(name, type, access, isStatic, isFinal);
        this.argDesc = checkNotNull(argDesc);
        this.arguments = checkNotNull(arguments);
        this.body = checkNotNull(body);
    }

    public String getArgDesc() {
        return argDesc;
    }

    public Iterable<J2xVariable> getArguments() {
        return arguments;
    }

    public J2xBlock getBody() {
        return body;
    }

    public boolean isConstructor() {
        return CONSTRUCTOR_NAME.equals(getName())
                || STATIC_CONSTRUCTOR_NAME.equals(getName());
    }
}
