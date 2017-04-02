package org.nibiru.j2cs;

import com.google.common.collect.Iterables;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

public class CsClass extends CsAccessFlags<JavaClass> {
    private CsClass(JavaClass clazz) {
        super(clazz);
    }

    public static CsClass wrap(JavaClass clazz) {
        checkNotNull(clazz);
        return new CsClass(clazz);
    }

    public String getNamespace() {
        return packageToNamespace(element.getPackageName());
    }

    public String getName() {
        int pos = element.getClassName().lastIndexOf('.');
        return pos < 0 ? element.getClassName() : element.getClassName().substring(pos + 1);
    }

    public Iterable<CsField> getFields() {
        return Iterables.transform(Arrays.asList(element.getFields()), this::wrap);
    }

    public Iterable<CsMethod> getMethods() {
        return Iterables.transform(Arrays.asList(element.getMethods()), this::wrap);
    }

    CsField wrap(Field field) {
        return new CsField(field, this);
    }

    CsMethod wrap(Method method) {
        return new CsMethod(method, this);
    }
}
