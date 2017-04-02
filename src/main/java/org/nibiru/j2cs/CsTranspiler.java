package org.nibiru.j2cs;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import org.apache.bcel.classfile.ClassParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class CsTranspiler {
    private final CsClass clazz;
    private PrintWriter out;
    private int indentation;

    public CsTranspiler(String path) {
        try {
            ClassParser parser = new ClassParser(path);
            clazz = CsClass.wrap(parser.parse());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(String path) {
        File file = new File(String.format("%s/%s.cs", path, clazz.getName()));
        file.getParentFile().mkdirs();
        try (PrintWriter out = new PrintWriter(new FileOutputStream(file))) {
            this.out = out;
            writeNamespace();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeNamespace() {
        out("namespace %s {", clazz.getNamespace());
        indent(this::writeClass);
        out("}");
    }

    private void writeClass() {
        out("%sclass %s {", modifiers(clazz), clazz.getName());
        indent(this::writeFields);
        indent(this::writeMethods);
        out("}");
    }

    private void writeFields() {
        for (CsField field : clazz.getFields()) {
            out("%s%s %s;", modifiers(field), field.getType(), field.getName());
        }
    }

    private void writeMethods() {
        for (CsMethod method : clazz.getMethods()) {
            out("%s%s %s(%s)", modifiers(method), method.getType(), method.getName(), Joiner.on(", ")
                    .join(Iterables.transform(method.getArguments(), (var) -> var.getType() + " " + var.getName())));
            out("{");
            indent(() -> writeMethodBody(method));
            out("}");
        }
    }

    private void writeMethodBody(CsMethod method) {
        for (String line : method.getCode()) {
            out(line + ";");
        }
    }

    private String modifiers(CsAccessFlags<?> accessFlags) {
        return String.format("%s%s%s",
                accessFlags.isPublic() ? "public " : "",
                accessFlags.isStatic() ? "static " : "",
                accessFlags.isReadonly() ? "readonly " : "");
    }

    private void out(String line, Object... args) {
        out.print(Strings.repeat("\t", indentation));
        out.println(String.format(line, args));
    }

    private void indent(Runnable callback) {
        indentation++;
        callback.run();
        indentation--;
    }
}
