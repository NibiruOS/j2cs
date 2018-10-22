package org.nibiru.j2x.cs;

import com.google.common.base.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.objectweb.asm.Type;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;

public class CsWritter {
    private static final Map<String, String> PREDEFINED_TYPES =
            ImmutableMap.of(Object.class.getName(), "object",
                    "string", "string",
                    "boolean", "bool");
    private static final Map<String, String> KEYWORD_SUBSTITUTION =
            ImmutableMap.of("unsafe", "_unsafe");
    private final Writer out;
    private final boolean pretty;
    private int indentation;

    public CsWritter(Writer out, boolean pretty) {
        this.out = checkNotNull(out);
        this.pretty = pretty;
    }

    public void write(CtClass<?> ctClass) {
        try {
            if (ctClass.getQualifiedName().equals(String.class.getName())) {
                updateStringClass(ctClass);
            }

            line("namespace %s", capitalize(ctClass.getPackage().getQualifiedName()));
            line("{");
            indentation++;
            line("%sclass %s%s", access(ctClass.getModifiers()),
                    keyword(ctClass.getSimpleName()),
                    (ctClass.getSuperclass() == null)
                            ? ""
                            : (" : " + capitalize(ctClass.getSuperclass().getQualifiedName())));
            line("{");
            indentation++;

            for (Object field : ctClass.getFields()) {
                write((CtField<?>) field);
            }

            for (CtConstructor<?> constructor : ctClass.getConstructors()) {
                write(ctClass, constructor);
            }

            for (CtMethod<?> method : ctClass.getMethods()) {
                write(ctClass, method);
            }

            indentation--;
            line("}");
            indentation--;
            line("}");
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void write(CtField<?> field) {
        line("%s%s %s;",
                modifiers(field),
                type(field.getType().getTypeDeclaration()),
                keyword(field.getSimpleName()));
    }

    private void write(CtClass<?> ctClass, CtConstructor<?> constructor) {
        line("%s%s(%s)    %s",
                modifiers(constructor),
                capitalize(ctClass.getSimpleName()),
                Joiner.on(", ")
                        .join(StreamSupport
                                .stream(constructor.getParameters().spliterator(), false)
                                .map(CsWritter::variable)
                                .collect(Collectors.toList())),
                " : base()");
        write_Body(constructor);

    }

    private void write(CtClass<?> ctClass, CtMethod<?> method) {
        line("%s%s%s(%s)",
                modifiers(method),
                type(method.getType().getTypeDeclaration()) + " ",
                capitalize(method.getSimpleName()),
                Joiner.on(", ").join(StreamSupport.stream(method
                        .getParameters()
                        .spliterator(), false)
                        .map(CsWritter::variable)
                        .collect(Collectors.toList())));
        write_Body(method);
    }

    private void write_Body(CtExecutable<?> executable) {
        line("{");
        indentation++;
        for (CtStatement statement : executable.getBody().getStatements()) {
            line(element(statement) + ";");
        }

        indentation--;

        line("}");
    }

    private static String element(CtCodeElement element) {
        if (element instanceof CtInvocation<?>) {
            return methodCallElement((CtInvocation<?>) element);
        } else if (element instanceof CtLiteral<?>) {
            return literalElement((CtLiteral<?>) element);
        } else if (element instanceof CtVariable<?>) {
            return variableElement((CtVariable<?>) element);
        } else if (element instanceof CtAssignment<?, ?>) {
            return assignmentElement((CtAssignment<?, ?>) element);
        } else if (element instanceof CtReturn<?>) {
            return returnElement((CtReturn<?>) element);
//        } else if (element instanceof ctNativeCode) {
//            return nativeCodeElement((ctNativeCode) element);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static String methodCallElement(CtInvocation<?> element) {
//        if (!isSuperCall(element)) {
        return (element.getExecutable()
                + "."
//                + capitalize(element.getLabel())
//                + "("
//                + buildArgs(element)
//                + ");"
        );
//        } else {
//            return null;
//        }
    }

    private static String literalElement(CtLiteral<?> element) {
        Object value = element.getValue();
        if (value == null) {
            return "null";
        } else if (value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Boolean) {
            return String.valueOf(value);
        } else if (value instanceof Long) {
            return value + "l";
        } else if (value instanceof Float) {
            return value + "f";
        } else if (value instanceof Double) {
            return value + "d";
        } else if (value instanceof Character) {
            Character charValue = (Character) value;
            return "'" + charValue + "'";
        } else if (value instanceof String) {
            String stringValue = (String) value;
            return "Java.Lang.String.FromNative(\"" + stringValue.replaceAll("%", "%%") + "\")";
        } else if (value instanceof Type) {
            Type typeValue = (Type) value;
            return "typeof(" + capitalize(typeValue.getClassName()) + ")";
        } else {
            throw new IllegalArgumentException("Literal value not supported (yet): " + value.getClass());
        }
    }

    private static String variableElement(CtVariable<?> element) {
        return element.getSimpleName();
    }

    private static String assignmentElement(CtAssignment<?, ?> element) {
        return element(element.getAssigned())
                + " = " + element(element.getAssignment());
    }

    private static String returnElement(CtReturn<?> element) {
        return "return" + (element.getReturnedExpression() != null
                ? " " + element(element.getReturnedExpression())
                : "");
    }

//    private static String nativeCodeElement(ctNativeCode element) {
//        return element.getCode();
//    }

//    private static ctMethodCall getSuperCall(ctBlock block) {
//        if (block.getElements().isEmpty()) {
//            return null;
//        } else {
//            Object element = block.getElements().get(0);
//            return isSuperCall(element)
//                    ? (ctMethodCall) element
//                    : null;
//        }
//    }

//    private static boolean isSuperCall(Object element) {
//        if (element instanceof ctMethodCall) {
//            ctMethodCall callSentence = (ctMethodCall) element;
//            return callSentence.getMethod().isConstructor();
//        } else {
//            return false;
//        }
//    }

    private static String buildArgs(CtInvocation<?> callSentence) {
        return Joiner.on(',')
                .join(Iterables.transform(callSentence.getArguments(),
                        CsWritter::element));
    }

    private static String variable(CtVariable<?> variable) {
        return String.format("%s %s",
                type(variable.getType().getTypeDeclaration()),
                keyword(variable.getSimpleName()));
    }

    private static String type(CtType<?> type) {
//        if (type instanceof ctArray) {
//            ctArray arrayType = (ctArray) type;
//            return type(arrayType.getItemClass()) + Strings.repeat("[]", arrayType.getDimensions());
//        } else {
        return PREDEFINED_TYPES.getOrDefault(type.getQualifiedName(), type.isPrimitive()
                ? type.getSimpleName()
                : capitalize(type.getQualifiedName()));
//        }
    }

    private static String access(Set<ModifierKind> access) {
        return access.contains(ModifierKind.PUBLIC) || (!access.contains(ModifierKind.PRIVATE) && !access.contains(ModifierKind.PROTECTED))
                ? "public "
                : "";
    }

    private static String modifiers(CtConstructor<?> method) {
        return (!method.isStatic()
                ? access(method.getModifiers())
                : "")
                + (method.isStatic()
                ? "static "
                : "");
    }

    private static String modifiers(CtMethod<?> method) {
        return access(method.getModifiers())
                + (method.isStatic()
                ? "static "
                : "");
        // + (method.isFinal() ? "sealed " : ""); // TODO: habria que ver todo el tema del virttual y todo eso. Por defecto es sealed, por lo que solo hay que especificarlo si se está sobreescribiendo un método virtual.
    }

    private static String modifiers(CtField<?> field) {
        return commonModifiers(field)
                + (field.isFinal() ? "readonly " : "");
    }

    private static String commonModifiers(CtTypeMember member) {
        return access(member.getModifiers())
                + (member.isStatic() ? "static " : "");
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
        } catch (Exception e) {
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

    private static String capitalize(String name) {
        return keyword(Joiner.on('.')
                .join(Iterables.transform(Splitter.on('.')
                        .split(name), CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL))));
    }

    public static String keyword(String name) {
        return MoreObjects.firstNonNull(KEYWORD_SUBSTITUTION.get(name),
                name).replaceAll("\\$", "_");
    }

    private static void updateStringClass(CtClass ctClass) {
//        ctBlock body = new ctBlock();
//        body.getElements()
//                .add(new ctReturn(new ctLiteral(null)));

//        ctClass.getMethods()
//                .add(new ctMethod("FromNative",
//                        ctClass,
//                        ctAccess.PUBLIC,
//                        true,
//                        true,
//                        "()V",
//                        ImmutableList.of(new ctVariable("value", new ctClass("string", "", null, ctAccess.PUBLIC))),
//                        body));
    }
}
