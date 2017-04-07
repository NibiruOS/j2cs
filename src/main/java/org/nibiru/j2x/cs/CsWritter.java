package org.nibiru.j2x.cs;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import org.nibiru.j2x.ast.J2xAccess;
import org.nibiru.j2x.ast.J2xClass;
import org.nibiru.j2x.ast.J2xConstructor;
import org.nibiru.j2x.ast.J2xField;
import org.nibiru.j2x.ast.J2xMember;
import org.nibiru.j2x.ast.J2xMethod;

import java.io.IOException;
import java.io.Writer;

import static com.google.common.base.Preconditions.checkNotNull;

public class CsWritter {
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
            line("%sclass %s {", access(j2xClass.getAccess()), j2xClass.getName());
            indentation++;

            for (J2xField field : j2xClass.getFields()) {
                write(field);
            }

            for (J2xMethod method : j2xClass.getMethods()) {
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

    private void write(J2xField field) {
        line("%s%s %s;",
                modifiers(field),
                field.getType().getFullName(),
                field.getName());
    }

    private void write(J2xMethod method) {
        line("%s%s%s(%s) {",
                modifiers(method),
                method instanceof J2xConstructor
                        ? ""
                        : method.getType().getFullName() + " ",
                capitalize(method.getName()),
                Joiner.on(',').join(method.getArguments()));
        line("}");
    }

    private String access(J2xAccess access) {
        if (access == J2xAccess.PUBLIC) {
            return "public ";
        } else {
            return "";
        }
    }
    private String modifiers(J2xMember member) {
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
