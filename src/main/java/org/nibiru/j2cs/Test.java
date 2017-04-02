package org.nibiru.j2cs;


import java.io.IOException;
import java.io.PrintWriter;

public class Test {
    public static void main(String[] args) throws IOException {
        CsClassProcessor processor = new CsClassProcessor(input -> new PrintWriter(System.out),
                "org/nibiru/j2cs/Hola");
        processor.process();
    }
}
