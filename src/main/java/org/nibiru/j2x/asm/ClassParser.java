package org.nibiru.j2x.asm;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.nibiru.j2x.DescIterable;
import org.nibiru.j2x.ast.J2xAccess;
import org.nibiru.j2x.ast.J2xArray;
import org.nibiru.j2x.ast.J2xBlock;
import org.nibiru.j2x.ast.J2xClass;
import org.nibiru.j2x.ast.J2xConstructor;
import org.nibiru.j2x.ast.J2xField;
import org.nibiru.j2x.ast.J2xMethod;
import org.nibiru.j2x.ast.J2xVariable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class ClassParser extends ClassVisitor {
    private static Map<String, J2xClass> systemClasses = Maps.newHashMap();

    static {
        try {
            for (Field field : J2xClass.class.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(J2xClass.class)) {
                    J2xClass value = (J2xClass) field.get(null);

                    systemClasses.put((value.getPackageName().isEmpty()
                            ? ""
                            : value.getPackageName().replaceAll("\\.", "/")
                            + "/") + value.getName(), value);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, J2xClass> parse(String classPath) {
        Map<String, J2xClass> generatedClasses = Maps.newHashMap();

        parseClassPath(classPath, generatedClasses);

        return generatedClasses;
    }

    private static J2xClass parseClassPath(String classPath, Map<String, J2xClass> generatedClasses) {
        J2xClass systemClass = systemClasses.get(classPath);
        if (systemClass != null) {
            return systemClass;
        }
        J2xClass generatedClass = generatedClasses.get(classPath);
        if (generatedClass != null) {
            return generatedClass;
        }
        if (classPath.endsWith("[]")) {
            String itemClassPath = classPath.substring(0, classPath.length() - 2);
            J2xArray array = new J2xArray(parseClassPath(itemClassPath, generatedClasses));
            systemClasses.put(classPath, array);
            return array;
        } else {
            return new ClassParser(generatedClasses).parseInternal(classPath);
        }
    }

    private final Map<String, J2xClass> generatedClasses;
    private J2xClass j2xClass;

    private ClassParser(Map<String, J2xClass> generatedClasses) {
        super(Opcodes.ASM5);
        this.generatedClasses = generatedClasses;
    }

    private J2xClass parseDesc(String desc) {
        return parseClassPath(descToPath(desc), generatedClasses);
    }

    private J2xClass parseInternal(String classPath) {
        try {
            ClassReader reader = new ClassReader(Class.class
                    .getResourceAsStream(String.format("/%s.class", classPath)));
            reader.accept(this, 0);
            return generatedClasses.get(classPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(int version,
                      int access,
                      String name,
                      String signature,
                      String superName,
                      String[] interfaces) {
        int pos = name.lastIndexOf("/");
        String packageName = name.substring(0, pos).replaceAll("/", ".");
        j2xClass = new J2xClass(name.substring(pos + 1),
                packageName,
                generatedClasses.get(superName),
                access(access));
        generatedClasses.put(name, j2xClass);
    }

    @Override
    public FieldVisitor visitField(int access,
                                   String name,
                                   String desc,
                                   String signature,
                                   Object value) {
        j2xClass.getFields().add(new J2xField(name,
                parseDesc(desc),
                access(access),
                isStatic(access),
                isFinal(access)));
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access,
                                     String name,
                                     String desc,
                                     String signature,
                                     String[] exceptions) {
        return new CsMethodVisitor(access,
                name,
                desc,
                signature,
                exceptions);
    }

    private static boolean isStatic(int access) {
        return (access & Opcodes.ACC_STATIC) != 0;
    }

    private static boolean isFinal(int access) {
        return (access & Opcodes.ACC_FINAL) != 0;
    }

    private static J2xAccess access(int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0) {
            return J2xAccess.PUBLIC;
        } else if ((access & Opcodes.ACC_PROTECTED) != 0) {
            return J2xAccess.PROTECTED;
        } else if ((access & Opcodes.ACC_PRIVATE) != 0) {
            return J2xAccess.PRIVATE;
        } else {
            return J2xAccess.DEFAULT;
        }
    }

    private String descToPath(String signature) {
        if (signature.startsWith("[")) {
            return descToPath(signature.substring(1)) + "[]";
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
                default:
                    return signature.substring(1,
                            signature.length() - 1);
            }
        }
    }


    private class CsMethodVisitor extends MethodVisitor {
        private final int access;
        private final String name;
        private final String desc;
        private final String signature;
        private final String[] exceptions;

        private final List<J2xVariable> arguments;
        private final J2xBlock body;

        private int argCount;
        private final Deque<String> stack;

        private CsMethodVisitor(int access,
                                String name,
                                String desc,
                                String signature,
                                String[] exceptions) {
            super(Opcodes.ASM5);
            this.access = access;
            this.name = name;
            this.desc = desc;
            this.signature = signature;
            this.exceptions = exceptions;

            arguments = Lists.newArrayList();
            body = new J2xBlock();

            stack = Lists.newLinkedList();
            argCount = argCount(desc);
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
                    arguments.add(new J2xVariable(name, parseDesc(desc)));
                } else {
//                    line("%s %s;",
//                            signatureToType(desc),
//                            name);
                }
            }
        }

        @Override
        public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
            super.visitFrame(type, nLocal, local, nStack, stack);
        }

        @Override
        public void visitInsn(int opcode) {
//            line("return;");
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
//            stack.push(formatConstant(operand));
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
//            switch (opcode) {
//                case Opcodes.GETSTATIC:
//                    startLine();
//                    write("%s.%s", capitalize(slashToDot(owner)), capitalize(name));
//                    return;
//                case Opcodes.PUTFIELD:
//                    String value = stack.pop();
//                    line("this.%s = %s;", name, value);
//                    return;
//            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
//            int argCount = argCount(desc);
//
//            List<String> args = Lists.newLinkedList();
//            for (int n = 0; n < argCount; n++) {
//                args.add(0, stack.pop());
//            }
//
//            boolean isConstructor = name.equals("<init>");
//            if (isConstructor) {
//                line("base(%s);", Joiner.on(',').join(args));
//            } else {
//                write(".%s(%s);", capitalize(name), Joiner.on(',').join(args));
//                endLine();
//            }
//            stack.clear();
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
//            stack.push(formatConstant(cst));
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
            J2xMethod method = name.equals("<init>")
                    ? new J2xConstructor(j2xClass.getName(),
                    J2xClass.VOID,
                    access(access),
                    isStatic(access),
                    isFinal(access),
                    arguments,
                    body)
                    : new J2xMethod(name,
                    parseDesc(returnType(desc)),
                    access(access),
                    isStatic(access),
                    isFinal(access),
                    arguments,
                    body);
            j2xClass.getMethods().add(method);
        }

        private int argCount(String desc) {
            return Iterables.size(new DescIterable(argTypes(desc)));
        }

        private String argTypes(String desc) {
            return desc.substring(1, desc.indexOf(')'));
        }

        private String returnType(String desc) {
            return desc.substring(desc.indexOf(')') + 1);
        }
    }
}