package org.nibiru.j2cs;

import com.google.common.base.Preconditions;

import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.generic.Type;

class CsFieldOrMethod<T extends FieldOrMethod> extends CsAccessFlags<T> {
    final CsClass clazz;

    public CsFieldOrMethod(T element, CsClass clazz) {
        super(element);
        this.clazz = Preconditions.checkNotNull(clazz);
    }

    public String getName() {
        return element.getName();
    }

    public String getType() {
        return signatureTotype(element.getSignature());
    }

    String signatureTotype(Type type) {
        return signatureTotype(type.getSignature());
    }

    String signatureTotype(String signature) {
        if (signature.startsWith("[")) {
            return signatureTotype(signature.substring(1)) + "[]";
        } else {
            switch (signature) {
                case "V":
                    return "";
                case "":
                    return "void";
                case "I":
                    return "int";
                case "Z":
                    return "boolean";
                case "Ljava/lang/String;":
                    return "string";
                default:
                    return packageToNamespace(signature.substring(1, signature.length() - 1).replaceAll("/", "."));
            }
        }
    }
}
