package org.nibiru.j2x.ast;

import com.google.common.collect.Lists;

import org.nibiru.j2x.ast.element.J2xElement;
import org.nibiru.j2x.ast.element.J2xVariable;

import java.util.Collection;
import java.util.List;

public class J2xBlock {
    private final List<J2xElement> elements;
    private final Collection<J2xVariable> variables;

    public J2xBlock() {
        this.elements = Lists.newArrayList();
        this.variables = Lists.newArrayList();
    }

    public List<J2xElement> getElements() {
        return elements;
    }

    public Collection<J2xVariable> getVariables() {
        return variables;
    }
}
