package org.nibiru.j2x.ast;

import com.google.common.collect.Lists;

import java.util.List;

public class J2xBlock {
    private final List<J2xSentence> sentences;

    public J2xBlock() {
        this.sentences = Lists.newArrayList();
    }

    public List<J2xSentence> getSentences() {
        return sentences;
    }
}
