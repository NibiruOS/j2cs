package org.nibiru.j2x.borrame;

import org.nibiru.j2x.ast.J2xNative;

public class Hola {
    public Hola() {
        /*
        String toto = "ABC";
        toto.toUpperCase();
        String caca ="pablo";
        System.out.println(caca.toString());
        */
        System.out.println("hola");
    }
/*
    private static int numero(int a) {
        return 789;
    }

    @J2xNative(language = "C#",
            value = "System.Console.WriteLine(\"Primero meto el nativo! \" + a);")
    //  TODO: un mejor enfoque seria meter una funcion estática que embeba el código nativo...
    // Entonces se podría meter inline, en cualquier parte del codigo!
    private static int numero(int a, int b) {
//        new String(nativeCode(String.class, "C#", "a+1"));
//        return 666 + nativeCode(Integer.class,"C#", "a+1");
        return 666;
    }

    @J2xNative(language = "C#",
            value = "System.Console.WriteLine(\"Hola!\");" +
                    "\nreturn 5+1;")
    public native int sumar(int valor); // TODO: no está reconociendo los parámetros en los métodos nativos
//    private static int numero(int a, int b) {
//        return 666;
//    }
    //    final String saludo = "hola";
//    public int kkck = 123;
//    public String[] otro;
//    public Date[][] fechas;
//    boolean zxing;
//
//    public void simple() {
//        System.out.println("hola");
//    }
//    public Hola(String pepito, boolean x) {
//        super();
//    }
//
//    public void saludar() {
    //boolean mal = true;
//        if (mal) {
//            return "Hola guampa";
//        } else {
//            return "Hola";
//        }
    //return "Hola";
//    }
//
//    static <T> T nativeCode(Class<T> clazz, String lang, String code) {
//        return null;
//    }
*/
}
