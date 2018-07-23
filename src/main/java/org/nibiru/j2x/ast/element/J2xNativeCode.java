package org.nibiru.j2x.ast.element;

import static com.google.common.base.Preconditions.checkNotNull;

public class J2xNativeCode {
    private final String language;
    private final String code;

    public J2xNativeCode(String language, String code) {
        this.language = checkNotNull(language);
        this.code = checkNotNull(code);
    }

    public String getLanguage() {
        return language;
    }

    public String getCode() {
        return code;
    }
}
