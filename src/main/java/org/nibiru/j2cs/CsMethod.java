package org.nibiru.j2cs;

import com.google.common.collect.Lists;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

import java.util.List;

public class CsMethod extends CsFieldOrMethod<Method> {
    CsMethod(Method method, CsClass clazz) {
        super(method, clazz);
    }

    public String getName() {
        if (element.getName().equals("<init>")) {
            return clazz.getName();
        } else {
            return super.getName();
        }
    }

    public String getType() {
        return signatureTotype(element.getReturnType().getSignature());
    }

    public Iterable<CsVariable> getArguments() {
        List<CsVariable> args = Lists.newArrayList();
        Type[] types = element.getArgumentTypes();
        LocalVariable[] vars = element.getCode().getLocalVariableTable().getLocalVariableTable();
        for (int n = 0; n < types.length; n++) {
            args.add(new CsVariable(vars[n + (element.isStatic() ? 0 : 1)].getName(), signatureTotype(types[n])));
        }
        return args;
    }

    public Iterable<String> getCode() {
        List<String> lines = Lists.newArrayList();
        LocalVariable[] vars = element.getCode().getLocalVariableTable().getLocalVariableTable();
        for (int n = 2; n < vars.length; n++) {
            LocalVariable var = vars[n];
            lines.add(String.format("%s %s", signatureTotype(var.getSignature()), var.getName()));
        }
        return lines;
    }

}
