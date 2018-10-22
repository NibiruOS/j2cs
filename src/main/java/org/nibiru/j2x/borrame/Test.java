package org.nibiru.j2x.borrame;


import com.google.common.base.CaseFormat;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import org.nibiru.j2x.cs.CsWritter;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;

import java.io.File;
import java.io.PrintWriter;

public class Test {
    public static void main(String[] args) throws Exception {
//        Map<String, J2xClass> result = ClassParser.parse("org/nibiru/j2x/borrame/Hola", new IgnoreSystemClassesPolicy());
//        for (J2xClass clazz : result.values()) {
//            //String path = "f:/workspace/core/";
//            String path = "/home/lbrasseur/workspace/core/";
//            File dir = new File(path + Joiner.on('/')
//                    .join(Iterables.transform(Splitter.on('.')
//                            .split(clazz.getPackageName()), CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL))));
//            dir.mkdirs();
//            new CsWritter(new PrintWriter(new File(dir, CsWritter.keyword(clazz.getName()) + ".cs")), true).write(clazz);
//        }

        CtClass<?> clazz = Launcher.parseClass(Files.toString(new File("src/main/java/org/nibiru/j2x/borrame/Hola.java"),
                Charsets.UTF_8));
        String path = "/home/lbrasseur/workspace/core/";
        File dir = new File(path + Joiner.on('/')
                .join(Iterables.transform(Splitter.on('.')
                                .split(clazz.getPackage().getQualifiedName()),
                        CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL))));
        dir.mkdirs();
        new CsWritter(new PrintWriter(new File(dir, CsWritter.keyword(clazz.getSimpleName()) + ".cs")), true).write(clazz);
    }
}
