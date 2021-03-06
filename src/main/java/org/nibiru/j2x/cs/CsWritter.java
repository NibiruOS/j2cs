package org.nibiru.j2x.cs;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import org.nibiru.j2x.ast.J2xAccess;
import org.nibiru.j2x.ast.J2xArray;
import org.nibiru.j2x.ast.J2xBlock;
import org.nibiru.j2x.ast.J2xClass;
import org.nibiru.j2x.ast.J2xField;
import org.nibiru.j2x.ast.J2xMember;
import org.nibiru.j2x.ast.J2xMethod;
import org.nibiru.j2x.ast.element.J2xAssignment;
import org.nibiru.j2x.ast.element.J2xLiteral;
import org.nibiru.j2x.ast.element.J2xMethodCall;
import org.nibiru.j2x.ast.element.J2xNativeCode;
import org.nibiru.j2x.ast.element.J2xReturn;
import org.nibiru.j2x.ast.element.J2xVariable;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
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

    public void write(J2xClass j2xClass) {
        try {
            if (j2xClass.getFullName().equals(String.class.getName())) {
                updateStringClass(j2xClass);
            }

            line("namespace %s", capitalize(j2xClass.getPackageName()));
            line("{");
            indentation++;
            line("%sclass %s%s", access(j2xClass.getAccess()),
                    keyword(j2xClass.getName()),
                    (j2xClass.getSuperClass() == null)
                            ? ""
                            : (" : " + capitalize(j2xClass.getSuperClass().getFullName())));
            line("{");
            indentation++;

            for (J2xField field : j2xClass.getFields()) {
                write(field);
            }

            for (J2xMethod method : j2xClass.getMethods()) {
                write(j2xClass, method);
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

    private void write(J2xField field) {
        line("%s%s %s;",
                modifiers(field),
                type(field.getType()),
                keyword(field.getName()));
    }

    private void write(J2xClass j2xClass, J2xMethod method) {
        J2xMethodCall superCall = getSuperCall(method.getBody());

        if (j2xClass.getName().equals("Hola") && superCall != null) {
            System.out.print(22);
        }

        line("%s%s%s(%s)    %s",
                modifiers(method),
                method.isConstructor()
                        ? ""
                        : type(method.getType()) + " ",
                capitalize(method.isConstructor()
                        ? j2xClass.getName()
                        : method.getName()),
                Joiner.on(", ").join(StreamSupport.stream(method.getArguments().spliterator(), false)
                        .map(CsWritter::variable)
                        .collect(Collectors.toList())),
                superCall != null
                        ? " : base(" + buildArgs(superCall) + ")"
                        : "");
        line("{");
        indentation++;
        for (J2xVariable variable : method.getBody().getVariables()) {
            if (!variable.isThis()) {
                line(type(variable.getType()) + " " + keyword(variable.getName()) + ";");
            }
        }
        for (Object element : method.getBody().getElements()) {
            String line = element(element);
            if (line != null) {
                line(line);
            }
        }

        indentation--;

        line("}");
    }

    private static String element(Object element) {
        if (element instanceof J2xMethodCall) {
            return methodCallElement((J2xMethodCall) element);
        } else if (element instanceof J2xLiteral) {
            return literalElement((J2xLiteral) element);
        } else if (element instanceof J2xVariable) {
            return variableElement((J2xVariable) element);
        } else if (element instanceof J2xAssignment) {
            return assignmentElement((J2xAssignment) element);
        } else if (element instanceof J2xReturn) {
            return returnElement((J2xReturn) element);
        } else if (element instanceof J2xNativeCode) {
            return nativeCodeElement((J2xNativeCode) element);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static String methodCallElement(J2xMethodCall element) {
        if (!isSuperCall(element)) {
            return (element.getTarget().getName()
                    + "."
                    + capitalize(element.getMethod().getName())
                    + "("
                    + buildArgs(element)
                    + ");");
        } else {
            return null;
        }
    }

    private static String literalElement(J2xLiteral element) {
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

    private static String variableElement(J2xVariable element) {
        return element.getName();
    }

    private static String assignmentElement(J2xAssignment element) {
        return element(element.getTarget()) + " = " + element(element.getValue()) + ";";
    }

    private static String returnElement(J2xReturn element) {
        return "return" + (element.getValue() != null
                ? " " + element(element.getValue())
                : "")
                + ";";
    }

    private static String nativeCodeElement(J2xNativeCode element) {
        return element.getCode();
    }

    private static J2xMethodCall getSuperCall(J2xBlock block) {
        if (block.getElements().isEmpty()) {
            return null;
        } else {
            Object element = block.getElements().get(0);
            return isSuperCall(element)
                    ? (J2xMethodCall) element
                    : null;
        }
    }

    private static boolean isSuperCall(Object element) {
        if (element instanceof J2xMethodCall) {
            J2xMethodCall callSentence = (J2xMethodCall) element;
            return callSentence.getMethod().isConstructor();
        } else {
            return false;
        }
    }

    private static String buildArgs(J2xMethodCall callSentence) {
        return Joiner.on(',')
                .join(Iterables.transform(callSentence.getArgs(),
                        CsWritter::element));
    }

    private static String variable(J2xVariable variable) {
        return String.format("%s %s",
                type(variable.getType()),
                keyword(variable.getName()));
    }

    private static String type(J2xClass type) {
        if (type instanceof J2xArray) {
            J2xArray arrayType = (J2xArray) type;
            return type(arrayType.getItemClass()) + Strings.repeat("[]", arrayType.getDimensions());
        } else {
            return PREDEFINED_TYPES.getOrDefault(type.getFullName(), type.isPrimitive()
                    ? type.getName()
                    : capitalize(type.getFullName()));
        }
    }

    private static String access(J2xAccess access) {
        return access == J2xAccess.PUBLIC || access == J2xAccess.DEFAULT
                ? "public "
                : "";
    }

    private static String modifiers(J2xMethod method) {
        return (!(method.isConstructor() && method.isStatic())
                ? access(method.getAccess())
                : "")
                + (method.isStatic()
                ? "static "
                : "");
        // + (method.isFinal() ? "sealed " : ""); // TODO: habria que ver todo el tema del virttual y todo eso. Por defecto es sealed, por lo que solo hay que especificarlo si se está sobreescribiendo un método virtual.
    }

    private static String modifiers(J2xField field) {
        return commonModifiers(field)
                + (field.isFinal() ? "readonly " : "");
    }

    private static String commonModifiers(J2xMember member) {
        return access(member.getAccess())
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

    private static void updateStringClass(J2xClass j2xClass) {
        J2xBlock body = new J2xBlock();
        body.getElements()
                .add(new J2xReturn(new J2xLiteral(null)));

        j2xClass.getMethods()
                .add(new J2xMethod("FromNative",
                        j2xClass,
                        J2xAccess.PUBLIC,
                        true,
                        true,
                        "()V",
                        ImmutableList.of(new J2xVariable("value", new J2xClass("string", "", null, J2xAccess.PUBLIC))),
                        body));
    }
}
