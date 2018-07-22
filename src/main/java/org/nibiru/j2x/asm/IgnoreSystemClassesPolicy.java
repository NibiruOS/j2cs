package org.nibiru.j2x.asm;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

public class IgnoreSystemClassesPolicy implements ParsePolicy {
    @Override
    public boolean mustParseContent(@Nonnull String className) {
        checkNotNull(className);
        return !className.startsWith("java.") && !className.startsWith("sun.");
    }
}
