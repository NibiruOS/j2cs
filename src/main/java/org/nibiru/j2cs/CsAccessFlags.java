package org.nibiru.j2cs;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import org.apache.bcel.classfile.AccessFlags;

import static com.google.common.base.Preconditions.checkNotNull;

class CsAccessFlags<T extends AccessFlags> {
    final T element;

    public CsAccessFlags(T element) {
        this.element = checkNotNull(element);
    }

    static String packageToNamespace(String packageName) {
        return Joiner.on('.').join(Iterables.transform(Splitter.on('.').split(packageName),
                CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL)));
    }

    public boolean isPublic() {
        return element.isPublic();
    }

    public boolean isReadonly() {
        return element.isFinal();
    }

    public boolean isStatic() {
        return element.isStatic();
    }
}
