package org.nibiru.j2x.borrame;


import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import org.nibiru.j2x.asm.ClassParser;
import org.nibiru.j2x.asm.IgnoreSystemClassesPolicy;
import org.nibiru.j2x.ast.J2xClass;
import org.nibiru.j2x.cs.CsWritter;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws Exception {
        Map<String, J2xClass> result = ClassParser.parse("org/nibiru/j2x/borrame/Hola", new IgnoreSystemClassesPolicy());
        for (J2xClass clazz : result.values()) {
            File dir = new File("f:/workspace/core/" + Joiner.on('/')
                    .join(Iterables.transform(Splitter.on('.')
                            .split(clazz.getPackageName()), CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL))));
            dir.mkdirs();
            new CsWritter(new PrintWriter(new File(dir, clazz.getName() + ".cs")), true).write(clazz);
        }
    }
}
