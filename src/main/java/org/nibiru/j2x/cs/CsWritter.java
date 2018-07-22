package org.nibiru.j2x.cs;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
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
import org.nibiru.j2x.ast.element.J2xReturn;
import org.nibiru.j2x.ast.element.J2xVariable;
import org.nibiru.j2x.ast.element.J2xMethodCall;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;

public class CsWritter {
    private static final Map<String, String> PREDEFINED_TYPES =
            ImmutableMap.of(Object.class.getName(), "object",
                    String.class.getName(), "string");
    private final Writer out;
    private final boolean pretty;
    private int indentation;

    public CsWritter(Writer out, boolean pretty) {
        this.out = checkNotNull(out);
        this.pretty = pretty;
    }

    public void write(J2xClass j2xClass) {
        try {
            line("namespace %s {", capitalize(j2xClass.getPackageName()));
            indentation++;
            line("%sclass %s%s {", access(j2xClass.getAccess()),
                    j2xClass.getName(),
                    (j2xClass.getSuperClass() == null || j2xClass.getSuperClass().equals(J2xClass.OBJECT)) ? "" : (" : " + capitalize(j2xClass.getSuperClass().getFullName())));
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
                field.getName());
    }

    private void write(J2xClass j2xClass, J2xMethod method) {


        J2xMethodCall superCall = getSuperCall(method.getBody());

        line("%s%s%s(%s) %s {",
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

        indentation++;
        for (J2xVariable variable : method.getBody().getVariables()) {
            if (!variable.isThis()) {
                line(type(variable.getType()) + " " + variable.getName() + ";");
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
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static String methodCallElement(J2xMethodCall element) {
        if (!isSuperCall(element)) {
            return (element.getTarget().getName()
                    + "."
                    + element.getMethod().getName()
                    + "("
                    + buildArgs(element)
                    + ");");
        } else {
            return null;
        }
    }

    private static String literalElement(J2xLiteral element) {
        if (element.getValue() == null) {
            return "null";
        } else if (element.getValue() instanceof Integer) {
            return String.valueOf(element.getValue());
        } else if (element.getValue() instanceof Float) {
            return element.getValue() + "f";
        } else if (element.getValue() instanceof Double) {
            return element.getValue() + "d";
        } else if (element.getValue() instanceof String) {
            return "\"" + element.getValue() + "\"";
        } else {
            throw new IllegalArgumentException("Value not supported (yet): " + element.getValue().getClass());
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
        return Joiner.on(',').join(Iterables.transform(callSentence.getArgs(), CsWritter::element));
    }

    private static String variable(J2xVariable variable) {
        return String.format("%s %s",
                type(variable.getType()),
                variable.getName());
    }

    private static String type(J2xClass type) {
        if (type instanceof J2xArray) {
            J2xArray arrayType = (J2xArray) type;
            return type(arrayType.getItemClass()) + Strings.repeat("[]", arrayType.getDimensions());
        } else {
            return type.isPrimitive() ?
                    type.getName()
                    : PREDEFINED_TYPES.getOrDefault(type.getFullName(), capitalize(type.getFullName()));
        }
    }

    private static String access(J2xAccess access) {
        if (access == J2xAccess.PUBLIC) {
            return "public ";
        } else {
            return "";
        }
    }

    private static String modifiers(J2xMember member) {
        return String.format("%s%s%s",
                access(member.getAccess()),
                member.isStatic() ? "static " : "",
                member.isFinal() ? "readonly " : "");
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
}
