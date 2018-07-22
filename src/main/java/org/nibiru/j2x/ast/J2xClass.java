package org.nibiru.j2x.ast;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import java.util.Collection;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class J2xClass {
    static String JAVA_LANG_PACKAGE = "java.lang";
    public static final J2xClass VOID = new J2xClass("void");
    public static final J2xClass BOOL = new J2xClass("bool");
    public static final J2xClass CHAR = new J2xClass("char");
    public static final J2xClass BYTE = new J2xClass("byte");
    public static final J2xClass SHORT = new J2xClass("short");
    public static final J2xClass INT = new J2xClass("int");
    public static final J2xClass FLOAT = new J2xClass("float");
    public static final J2xClass LONG = new J2xClass("long");
    public static final J2xClass DOUBLE = new J2xClass("double");
    public static final J2xClass OBJECT = buildObjectClass();

    private static J2xClass buildObjectClass() {
        J2xClass clazz = new J2xClass("Object", JAVA_LANG_PACKAGE);
        clazz.getMethods().add(new J2xMethod(J2xMethod.CONSTRUCTOR_NAME,
                J2xClass.VOID,
                J2xAccess.PUBLIC,
                false,
                false,
                "()V",
                ImmutableList.of(),
                new J2xBlock()));
        return clazz;
    }

//    public static final J2xClass STRING = new J2xClass("String", JAVA_LANG_PACKAGE);

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
        for (J2xMethod method : methods) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }
}
