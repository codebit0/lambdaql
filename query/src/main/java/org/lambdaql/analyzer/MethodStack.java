package org.lambdaql.analyzer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
public class MethodStack {
    private final IOperand owner;
    private final MethodSignature signature;
    private final IOperand[] args;
    private List<Object> stacks;

    @Setter
    private boolean entity = false;

    public MethodStack(IOperand owner, MethodSignature signature, IOperand[] args) {
        this.owner = owner;
        this.signature = signature;
        this.args = args;
    }


    public MethodStack addStack(MethodStack stack) {
        stacks.add(stack);
        return this;
    }

}
