package org.lambdaql.analyzer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
public class MethodStack {
    private final Object owner;
    private final MethodSignature signature;
    private final Object[] args;
    private List<Object> stacks = new ArrayList<>();

    @Setter
    private boolean entity = false;

    public MethodStack(Object owner, MethodSignature signature, Object[] args) {
        this.owner = owner;
        this.signature = signature;
        this.args = args;
    }


    public MethodStack addStack(MethodStack stack) {
        stacks.add(stack);
        return this;
    }

}
