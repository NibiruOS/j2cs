package org.nibiru.j2cs;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Set;

public class CsClassProcessor {
    private final Set<String> pending;
    private final Set<String> processed;
    private final Function<String, Writer> outputFactory;


    public CsClassProcessor(Function<String, Writer> outputFactory,
                            String... initialClasses) {
        pending = Sets.newHashSet();
        processed = Sets.newHashSet();
        this.outputFactory = Preconditions.checkNotNull(outputFactory);
        pending.addAll(Arrays.asList(initialClasses));
    }

    public void process() {
        while (pending.size() > 0) {
            process(pending.iterator().next());
        }
    }

    private void process(String clazz) {
        try {
            pending.remove(clazz);
            processed.add(clazz);
            ClassReader reader = new ClassReader(getClass().getResourceAsStream(String.format("/%s.class", clazz)));
            CsClassWritter writter = new CsClassWritter(outputFactory.apply(clazz), true, (referencedClass) -> {
                if (!processed.contains(referencedClass) && !referencedClass.startsWith("java/")) {
                    pending.add(referencedClass);
                }
            });
            reader.accept(writter, 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
