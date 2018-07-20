package org.nibiru.j2x.ast.element;

import org.nibiru.j2x.ast.J2xMethod;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class J2xMethodCall {
    private final J2xVariable target;
    private final J2xMethod method;
    private final List<Object> args;

    public J2xMethodCall(J2xVariable target,
                         J2xMethod method,
                         List<Object> args) {
        this.target = checkNotNull(target);
        this.method = checkNotNull(method);
        this.args = checkNotNull(args);
    }

    public J2xVariable getTarget() {
        return target;
    }

    public J2xMethod getMethod() {
        return method;
    }

    public List<Object> getArgs() {
        return args;
    }
}
