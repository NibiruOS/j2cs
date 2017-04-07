package org.nibiru.j2x;


import org.nibiru.j2x.asm.ClassParser;
import org.nibiru.j2x.ast.J2xClass;
import org.nibiru.j2x.cs.CsWritter;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class Test {
    public static void main2(String[] args) throws IOException {
        ClassReader reader = new ClassReader(Test.class.getResourceAsStream(String.format("/org/nibiru/j2x/Hola.class")));
        CsClassWritter writter = new CsClassWritter(new PrintWriter(System.out), true, (referencedClass) -> {
        });
        reader.accept(writter, 0);
    }

    public static void main1(String[] args) throws IOException {
        CsClassProcessor processor = new CsClassProcessor(input -> new PrintWriter(System.out),
                "org/nibiru/j2x/Hola");
        processor.process();
    }

    public static void main(String[] args) throws IOException {
        Map<String, J2xClass> result = ClassParser.parse("org/nibiru/j2x/Hola");
        CsWritter writter = new CsWritter(new PrintWriter(System.out), true);
        for (J2xClass clazz : result.values()) {
            writter.write(clazz);
        }
    }
}
