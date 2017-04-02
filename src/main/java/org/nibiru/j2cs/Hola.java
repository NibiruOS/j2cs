package org.nibiru.j2cs;

import com.google.common.base.Preconditions;

public class Hola {
    final String saludo = "hola";
    public int kkck = 123;
    boolean zxing;
    Preconditions p;
    public Hola(Preconditions[] p, boolean x) {
        super();
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
