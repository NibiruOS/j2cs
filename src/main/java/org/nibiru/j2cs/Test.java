package org.nibiru.j2cs;


import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.PrintWriter;

public class Test {
    public static void main(String[] args) throws IOException {
        ClassReader reader = new ClassReader(Test.class.getResourceAsStream(String.format("/org/nibiru/j2cs/Hola.class")));
        CsClassWritter writter = new CsClassWritter(new PrintWriter(System.out), true, (referencedClass) -> {
        });
        reader.accept(writter, 0);
    }

    public static void main1(String[] args) throws IOException {
        CsClassProcessor processor = new CsClassProcessor(input -> new PrintWriter(System.out),
                "org/nibiru/j2cs/Hola");
        processor.process();
    }
}
