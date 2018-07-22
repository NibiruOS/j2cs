package org.nibiru.j2x.asm;

import javax.annotation.Nonnull;

public interface ParsePolicy {
    boolean mustParseContent(@Nonnull String className);
}
