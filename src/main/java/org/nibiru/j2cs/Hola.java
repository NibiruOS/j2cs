package org.nibiru.j2cs;

import com.google.common.base.Preconditions;

import java.io.IOException;

public class Hola {
    final String saludo = "hola";
    public int kkck = 123;
    boolean zxing;
    Preconditions p;
    public Hola(Preconditions p) {
        super();
        this.p = p;
    }

    public static void main(String[] args) throws IOException {
        new CsTranspiler("build/classes/main/org/nibiru/j2cs/Hola.class").write("build/cs");
    }

    public String saludar(boolean bien) {
        boolean mal = !bien;
        if (mal) {
            return "Hola guampa";
        } else {
            return "Hola";
        }
    }
}
