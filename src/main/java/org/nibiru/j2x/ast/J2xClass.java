package org.nibiru.j2x.ast;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import java.util.Collection;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class J2xClass {
    static String JAVA_LANG_PACKAGE = "java.lang";
    public static final J2xClass VOID = new J2xClass("void");
    public static final J2xClass BOOLEAN = new J2xClass("boolean");
    public static final J2xClass CHAR = new J2xClass("char");
    public static final J2xClass BYTE = new J2xClass("byte");
    public static final J2xClass SHORT = new J2xClass("short");
    public static final J2xClass INT = new J2xClass("int");
    public static final J2xClass FLOAT = new J2xClass("float");
    public static final J2xClass LONG = new J2xClass("long");
    public static final J2xClass DOUBLE = new J2xClass("double");

    private final String name;
    private final String packageName;
    private final J2xClass superClass;
    private final J2xAccess access;
    private boolean isPrimitive;
    private final Collection<J2xField> fields;
    private final Collection<J2xMethod> methods;

    private J2xClass(String name) {
        this(name, "", null, J2xAccess.PUBLIC, true);
    }

    private J2xClass(String name,
                     String packageName) {
        this(name, packageName, null, J2xAccess.PUBLIC);
    }

    public J2xClass(String name,
                    String packageName,
                    @Nullable J2xClass superClass,
                    J2xAccess access) {
        this(name,
                packageName,
                superClass,
                access,
                false);
    }

    private J2xClass(String name,
                     String packageName,
                     @Nullable J2xClass superClass,
                     J2xAccess access,
                     boolean isPrimitive) {
        this.name = checkNotNull(name);
        this.packageName = checkNotNull(packageName);
        this.superClass = superClass;
        this.access = checkNotNull(access);
        this.isPrimitive = isPrimitive;
        this.fields = Sets.newHashSet();
        this.methods = Sets.newHashSet();
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    @Nullable
    public J2xClass getSuperClass() {
        return superClass;
    }

    public J2xAccess getAccess() {
        return access;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public Collection<J2xField> getFields() {
        return fields;
    }

    public Collection<J2xMethod> getMethods() {
        return methods;
    }

    public String getFullName() {
        return (Strings.isNullOrEmpty(packageName) ? "" : (packageName + ".")) + name;
    }

    public J2xMethod findMethod(String name, String desc) {
        String argOnlyDesc = argOnlyDesc(desc);

        for (J2xMethod method : methods) {
            if (method.getName().equals(name)
                    && argOnlyDesc.equals(argOnlyDesc(method.getArgDesc()))) {
                return method;
            }
        }
        return null;
    }

    private static String argOnlyDesc(String desc) {
        return desc.substring(desc.indexOf('(') + 1, desc.indexOf(')'));
    }

    public boolean isAssignableFrom(J2xClass type) {
        return this.equals(type)
                || superClass != null
                && superClass.isAssignableFrom(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        J2xClass j2xClass = (J2xClass) o;
        return Objects.equal(name, j2xClass.name) &&
                Objects.equal(packageName, j2xClass.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, packageName);
    }
}
