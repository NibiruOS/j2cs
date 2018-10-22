package org.nibiru.j2x.cs;

import com.google.common.base.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.objectweb.asm.Type;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class CsWritter {
    public final static String CONSTRUCTOR_NAME = "<init>";
    public final static String STATIC_CONSTRUCTOR_NAME = "<clinit>";

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
                write(method);
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
                type(field.getType()),
                keyword(field.getSimpleName()));
    }

    private void write(CtClass<?> ctClass, CtConstructor<?> constructor) {
        CtInvocation<?> superCall = getSuperCall(constructor.getBody());

        line("%s%s(%s)%s",
                modifiers(constructor),
                capitalize(ctClass.getSimpleName()),
                Joiner.on(", ")
                        .join(constructor.getParameters().stream()
                                .map(CsWritter::variable)
                                .collect(Collectors.toList())),
                superCall != null
                        ? " : base(" + buildArgs(superCall) + ")"
                        : "");
        writeBody(constructor);

    }

    private void write(CtMethod<?> method) {
        line("%s%s%s(%s)",
                modifiers(method),
                type(method.getType()) + " ",
                capitalize(method.getSimpleName()),
                Joiner.on(", ").join(method
                        .getParameters()
                        .stream()
                        .map(CsWritter::variable)
                        .collect(Collectors.toList())));
        writeBody(method);
    }

    private void writeBody(CtExecutable<?> executable) {
        line("{");
        indentation++;
        for (CtStatement statement : executable.getBody().getStatements()) {
            String sentence = element(statement);
            if (sentence != null) {
                line(sentence + ";");
            }
        }

        indentation--;

        line("}");
    }

    private static String element(CtCodeElement element) {
        if (element instanceof CtInvocation<?>) {
            return invocationElement((CtInvocation<?>) element);
        } else if (element instanceof CtLiteral<?>) {
            return literalElement((CtLiteral<?>) element);
        } else if (element instanceof CtLocalVariable<?>) { // Local variable es subclase de variable
            return localVariableElement((CtLocalVariable<?>) element);
        } else if (element instanceof CtVariable<?>) {
            return variableElement((CtVariable<?>) element);
        } else if (element instanceof CtAssignment<?, ?>) {
            return assignmentElement((CtAssignment<?, ?>) element);
        } else if (element instanceof CtReturn<?>) {
            return returnElement((CtReturn<?>) element);
        } else if (element instanceof CtFieldRead<?>) { // Field read es un caso mas particular de variableread
            return fieldReadElement((CtFieldRead<?>) element);
        } else if (element instanceof CtVariableWrite<?>) {
            return variableWriteElement((CtVariableWrite<?>) element);
        } else if (element instanceof CtVariableRead<?>) {
            return variableReadElement((CtVariableRead<?>) element);
        } else if (element instanceof CtTypeAccess<?>) {
            return typeAccessElement((CtTypeAccess<?>) element);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static String variableReadElement(CtVariableRead<?> element) {
        return element.getVariable().getSimpleName();
    }

    private static String typeAccessElement(CtTypeAccess<?> element) {
        return type(element.getAccessedType());
    }

    private static String fieldReadElement(CtFieldRead<?> element) {
        return target(element)
                + element.getVariable().getSimpleName();
    }

    private static String variableWriteElement(CtVariableWrite<?> element) {
        return element.getVariable().getSimpleName();
    }

    private static String invocationElement(CtInvocation<?> element) {
        if (!isSuperCall(element)) {
            return target(element)
                    + capitalize(element.getExecutable().getSimpleName()
                    + "("
                    + buildArgs(element)
                    + ")");
        } else {
            return null;
        }
    }

    private static String target(CtTargetedExpression<?, ?> element) {
        return element.getTarget() != null
                ? element(element.getTarget()) + "."
                : "";
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

    private static String localVariableElement(CtLocalVariable<?> element) {
        return type(element.getType())
                + " "
                + variableElement(element);
    }

    private static String variableElement(CtVariable<?> element) {
        return element.getSimpleName() +
                (element.getDefaultExpression() != null
                        ? " = " + element(element.getDefaultExpression())
                        : "");
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

    private static CtInvocation<?> getSuperCall(CtBlock<?> block) {
        if (block.getStatements().isEmpty()) {
            return null;
        } else {
            Object element = block.getStatements().get(0);
            return (element instanceof CtInvocation<?>) && isSuperCall((CtInvocation<?>) element)
                    ? (CtInvocation<?>) element
                    : null;
        }
    }

    private static boolean isSuperCall(CtInvocation<?> element) {
        return element.getExecutable().isStatic()
                ? element.getExecutable().getSimpleName().equals(STATIC_CONSTRUCTOR_NAME)
                : element.getExecutable().getSimpleName().equals(CONSTRUCTOR_NAME);
    }

    private static String buildArgs(CtInvocation<?> callSentence) {
        return Joiner.on(',')
                .join(Iterables.transform(callSentence.getArguments(),
                        CsWritter::element));
    }

    private static String variable(CtVariable<?> variable) {
        return String.format("%s %s",
                type(variable.getType()),
                keyword(variable.getSimpleName()));
    }

    private static String type(CtTypeReference<?> type) {
//        if (type instanceof ctArray) {
//            ctArray arrayType = (ctArray) type;
//            return type(arrayType.getItemClass()) + Strings.repeat("[]", arrayType.getDimensions());
//        } else {
        return PREDEFINED_TYPES.getOrDefault(type.getQualifiedName(),
                type.isPrimitive()
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
