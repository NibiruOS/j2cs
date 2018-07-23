package org.nibiru.j2x.ast;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.nibiru.j2x.ast.element.J2xVariable;

import java.util.Collection;
import java.util.List;

public class J2xBlock {
    private final List<Object> elements;
    private final Collection<J2xVariable> variables;

    public J2xBlock() {
        this.elements = Lists.newArrayList();
        this.variables = Lists.newArrayList();
    }

    public List<Object> getElements() {
        return elements;
    }

    public Collection<J2xVariable> getVariables() {
        return variables;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        J2xBlock j2xBlock = (J2xBlock) o;
        return Objects.equal(elements, j2xBlock.elements) &&
                Objects.equal(variables, j2xBlock.variables);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(elements, variables);
    }
}
