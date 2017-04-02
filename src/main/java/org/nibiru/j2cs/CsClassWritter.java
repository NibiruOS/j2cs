package org.nibiru.j2cs;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.Writer;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public class CsClassWritter extends ClassVisitor {
    private final Writer out;
    private final boolean pretty;
    private final Consumer<String> dependencyCallback;
    private String classsName;
    private int indentation;

    public CsClassWritter(Writer out,
                          boolean pretty,
                          Consumer<String> dependencyCallback) {
        super(Opcodes.ASM5);
        this.out = checkNotNull(out);
        this.pretty = pretty;
        this.dependencyCallback = checkNotNull(dependencyCallback);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        int pos = name.lastIndexOf("/");
        String packageName = name.substring(0, pos).replaceAll("/", ".");
        classsName = name.substring(pos + 1);

        line("namespace %s {", capitalize(packageName));
        indentation++;
        line("%sclass %s {", modifiers(access), classsName);
        indentation++;
    }

    @Override
    public void visitEnd() {
        try {
            indentation--;
            line("}");
            indentation--;
            line("}");
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        line("%s%s %s;",
                modifiers(access),
                signatureTotype(desc),
                name);
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        int argEnd = desc.indexOf(')');
        int argCount = Iterables.size(new DescIterable(desc.substring(1, argEnd)));

        startLine();
        boolean isConstructor = name.equals("<init>");
        write("%s%s%s(",
                modifiers(access),
                isConstructor ? "" : signatureTotype(desc.substring(argEnd + 1)) + " ",
                isConstructor ? classsName : capitalize(name));
        return new CsMethodVisitor(argCount);
    }

    private void startLine() {
        try {
            if (pretty) {
                out.append(Strings.repeat("\t", indentation));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void write(String format, Object... args) {
        try {
            out.append(String.format(format, args));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void endLine() {
        try {
            if (pretty) {
                out.append("\r\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void line(String line, Object... args) {
        startLine();
        write(line, args);
        endLine();
    }

    private static String capitalize(String packageName) {
        return Joiner.on('.')
                .join(Iterables.transform(Splitter.on('.')
                        .split(packageName), CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL)));
    }

    private static String modifiers(int accessFlags) {
        return String.format("%s%s%s",
                (accessFlags & Opcodes.ACC_PUBLIC) != 0 ? "public " : "",
                (accessFlags & Opcodes.ACC_STATIC) != 0 ? "static " : "",
                (accessFlags & Opcodes.ACC_FINAL) != 0 ? "readonly " : "");
    }

    private String signatureTotype(String signature) {
        if (signature.startsWith("[")) {
            return signatureTotype(signature.substring(1)) + "[]";
        } else {
            switch (signature) {
                case "V":
                    return "void";
                case "Z":
                    return "boolean";
                case "C":
                    return "char";
                case "B":
                    return "byte";
                case "S":
                    return "short";
                case "I":
                    return "int";
                case "F":
                    return "float";
                case "J":
                    return "long";
                case "D":
                    return "double";
                case "Ljava/lang/Object;":
                    return "object";
                case "Ljava/lang/String;":
                    return "string";
                default:
                    String referencedClass = signature.substring(1,
                            signature.length() - 1);
                    dependencyCallback.accept(referencedClass);
                    return capitalize(referencedClass
                            .replaceAll("/", "."));
            }
        }
    }


    private class CsMethodVisitor extends MethodVisitor {
        private int argCount;

        public CsMethodVisitor(int argCount) {
            super(Opcodes.ASM5);
            this.argCount = argCount;
            writeParamsEnd();
        }

        public void visitLocalVariable(String name,
                                       String desc,
                                       String signature,
                                       Label start,
                                       Label end,
                                       int index) {
            if (!name.equals("this")) {
                if (argCount > 0) {
                    argCount--;
                    write("%s %s%s",
                            signatureTotype(desc),
                            name,
                            argCount > 0 ? ", " : "");
                    writeParamsEnd();
                } else {
                    line("%s %s;",
                            signatureTotype(desc),
                            name);
                }
            }
        }

        @Override
        public void visitEnd() {
            indentation--;
            line("}");
        }

        private void writeParamsEnd() {
            if (argCount == 0) {
                write(")");
                endLine();
                line("{");
                indentation++;
            }
        }
    }
}
