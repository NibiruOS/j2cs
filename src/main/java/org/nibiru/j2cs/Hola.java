package org.nibiru.j2cs;

import com.google.common.base.Preconditions;

import org.objectweb.asm.ClassReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;


public class Hola {
    final String saludo = "hola";
    public int kkck = 123;
    boolean zxing;
    Preconditions p;
    public Hola(Preconditions[] p, boolean x) {
        super();
    }

    public static void main(String[] args) throws IOException {
        ClassReader reader = new ClassReader(new FileInputStream("build/classes/main/org/nibiru/j2cs/Hola.class"));
        StringWriter sw = new StringWriter();
        CsClassWritter writter = new CsClassWritter(sw, true);
        reader.accept(writter, 0);
        System.out.println(sw.toString());
    }

    public <T> String saludar(boolean bien, T otro) {
        boolean mal = !bien;
        if (mal) {
            return "Hola guampa";
        } else {
            return "Hola";
        }
    }
}
