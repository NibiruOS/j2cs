package org.nibiru.j2cs;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import java.io.IOException;
import java.io.Writer;
import java.util.Deque;
import java.util.List;
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
                signatureToType(desc),
                name);
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        int argCount = argCount(desc);

        startLine();
        boolean isConstructor = name.equals("<init>");
        write("%s%s%s(",
                modifiers(access),
                isConstructor ? "" : signatureToType(returnType(desc)) + " ",
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

    private static String argTypes(String desc) {
        return desc.substring(1, desc.indexOf(')'));
    }

    private static String returnType(String desc) {
        return desc.substring(desc.indexOf(')') + 1);
    }

    private static int argCount(String desc) {
        return Iterables.size(new DescIterable(argTypes(desc)));
    }


    private static String formatConstant(Object constant) {
        if (constant == null) {
            return "null";
        } else if (constant instanceof String) {
            return String.format("\"%s\"", constant);
        } else {
            return constant.toString();
        }
    }

    private static String slashToDot(String path) {
        return path.replaceAll("/", ".");
    }

    private String signatureToType(String signature) {
        if (signature.startsWith("[")) {
            return signatureToType(signature.substring(1)) + "[]";
        } else {
            switch (signature) {
                case "V":
                    return "void";
                case "Z":
                    return "bool";
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
                    return capitalize(slashToDot(referencedClass));
            }
        }
    }


    private class CsMethodVisitor extends MethodVisitor {
        private int argCount;
        private Deque<String> stack;

        public CsMethodVisitor(int argCount) {
            super(Opcodes.ASM5);
            this.argCount = argCount;
            stack = Lists.newLinkedList();
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
                            signatureToType(desc),
                            name,
                            argCount > 0 ? ", " : "");
                    writeParamsEnd();
                } else {
                    line("%s %s;",
                            signatureToType(desc),
                            name);
                }
            }
        }

        @Override
        public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
            super.visitFrame(type, nLocal, local, nStack, stack);
        }

        @Override
        public void visitInsn(int opcode) {
            line("return;");
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            stack.push(formatConstant(operand));
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            super.visitVarInsn(opcode, var);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            switch (opcode) {
                case Opcodes.GETSTATIC:
                    startLine();
                    write("%s.%s", capitalize(slashToDot(owner)), capitalize(name));
                    return;
                case Opcodes.PUTFIELD:
                    String value = stack.pop();
                    line("this.%s = %s;", name, value);
                    return;
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            int argCount = argCount(desc);

            List<String> args = Lists.newLinkedList();
            for (int n=0;n<argCount;n++) {
                args.add(0, stack.pop());
            }

            boolean isConstructor = name.equals("<init>");
            if (isConstructor) {
                line("base(%s);", Joiner.on(',').join(args));
            } else {
                write(".%s(%s);", capitalize(name), Joiner.on(',').join(args));
                endLine();
            }
            stack.clear();
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
            super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            super.visitJumpInsn(opcode, label);
        }

        @Override
        public void visitLabel(Label label) {
            super.visitLabel(label);
        }

        @Override
        public void visitLdcInsn(Object cst) {
            stack.push(formatConstant(cst));
        }

        @Override
        public void visitIincInsn(int var, int increment) {
            super.visitIincInsn(var, increment);
        }

        @Override
        public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
            super.visitTableSwitchInsn(min, max, dflt, labels);
        }

        @Override
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
            super.visitLookupSwitchInsn(dflt, keys, labels);
        }

        @Override
        public void visitMultiANewArrayInsn(String desc, int dims) {
            super.visitMultiANewArrayInsn(desc, dims);
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            return super.visitInsnAnnotation(typeRef, typePath, desc, visible);
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            super.visitTryCatchBlock(start, end, handler, type);
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
            return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
        }

        @Override
        public void visitLineNumber(int line, Label start) {
//            if (!stack.isEmpty()) {
//                line("%s;", Joiner.on("").join(stack));
//            }
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack, maxLocals);
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
