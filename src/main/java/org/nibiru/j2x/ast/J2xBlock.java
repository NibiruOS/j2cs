package org.nibiru.j2x.ast;

import com.google.common.collect.Lists;

import org.nibiru.j2x.ast.element.J2xVariable;
import org.nibiru.j2x.ast.sentence.J2xSentence;

import java.util.Collection;
import java.util.List;

public class J2xBlock {
    private final List<J2xSentence> sentences;
    private final Collection<J2xVariable> variables;

    public J2xBlock() {
        this.sentences = Lists.newArrayList();
        this.variables = Lists.newArrayList();
    }

    public List<J2xSentence> getSentences() {
        return sentences;
    }

    public Collection<J2xVariable> getVariables() {
        return variables;
    }
}
