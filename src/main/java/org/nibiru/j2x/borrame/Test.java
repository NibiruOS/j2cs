package org.nibiru.j2x.borrame;


import com.google.common.base.CaseFormat;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.nibiru.j2x.cs.CsWritter;
import spoon.Launcher;
import spoon.SpoonException;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

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

        parse(Lists.newLinkedList(ImmutableList.of("org.nibiru.j2x.borrame.Hola")));
    }

    private static void parse(Deque<String> pending) {
        Set<String> classes = Sets.newHashSet();
        while (!pending.isEmpty()) {
            String className = pending.pop();
            if (!classes.contains(className)) {
                try {
                    System.out.println("Procesando " + className);

                    CtClass<?> clazz = Launcher.parseClass(Files.toString(new File("src/x/"
                                    + Joiner.on('/')
                                    .join(Splitter.on('.')
                                            .split(className))
                                    + ".java"),
                            Charsets.UTF_8));
                    classes.add(className);

                    if (clazz.getSuperclass() != null) {
                        enqueue(clazz.getSuperclass(), pending);
                    }
                    for (CtTypeReference<?> type : clazz.getSuperInterfaces()) {
                        enqueue(type, pending);
                    }
                    for (CtField<?> field : clazz.getFields()) {
                        enqueue(field.getType(), pending);
                    }
                    for (CtExecutable<?> method : Iterables.concat(clazz.getConstructors(),
                            clazz.getMethods())) {
                        for (CtTypeReference<?> type : method.getReferencedTypes()) {
                            enqueue(type, pending);
                        }
                    }

                    write(clazz);
                } catch (SpoonException | IOException e) {
                    // Sigo con el archivo siguiente
                }
            }
        }
    }

    private static void write(CtClass<?> clazz) throws FileNotFoundException {
        String path = "/home/lbrasseur/workspace/core/";
        File dir = new File(path + Joiner.on('/')
                .join(Iterables.transform(Splitter.on('.')
                                .split(clazz.getPackage().getQualifiedName()),
                        CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL))));
        dir.mkdirs();
        new CsWritter(new PrintWriter(new File(dir, CsWritter.keyword(clazz.getSimpleName()) + ".cs")), true)
                .write(clazz);
    }

    private static void enqueue(CtTypeReference<?> type, Deque<String> pending) {
        if (!type.isPrimitive()) {
            pending.push(type.getQualifiedName());
        }
    }
}
