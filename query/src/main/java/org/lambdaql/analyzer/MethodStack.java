package org.lambdaql.analyzer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
public class MethodStack {
    private MethodStack parent;
    private final Object owner;
    private final MethodSignature signature;
    private final Object[] args;
    private List<Object> stacks = new ArrayList<>();

    private boolean includeEntityVariable = false;
    private boolean includeCapturedVariable = false;

    @Setter
    private boolean ownerEntityVariable = false;
    @Setter
    private boolean ownerCapturedVariable = false;

    public MethodStack(Object owner, MethodSignature signature, Object[] args) {
        this.owner = owner;
        this.signature = signature;
        this.args = args;
    }

    public MethodStack addStack(MethodStack stack) {
        stack.parent = this;
        stacks.add(stack);
        if (stack.includeEntityVariable()) {
            includeEntityVariable(true);
        }
        return this;
    }

    public void includeEntityVariable(boolean isEntity) {
        this.includeEntityVariable = isEntity;
        if (parent != null) {
            parent.includeEntityVariable = isEntity;
        }
    }

    public void includeCapturedVariable(boolean isEntity) {
        this.includeEntityVariable = isEntity;
        if (parent != null) {
            parent.includeCapturedVariable = isEntity;
        }
    }
}
