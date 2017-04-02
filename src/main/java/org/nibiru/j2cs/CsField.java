package org.nibiru.j2cs;

import org.apache.bcel.classfile.Field;

public class CsField extends CsFieldOrMethod<Field> {
    CsField(Field field, CsClass clazz) {
        super(field, clazz);
    }
}
