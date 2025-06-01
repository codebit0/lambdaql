package org.lambdaql.analyzer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
public class MethodStack {
    @Setter
    private MethodStack parent;
    private final Object owner;
    private final MethodSignature signature;
    private final Object[] args;
    private List<MethodStack> stacks = new ArrayList<>();

    private boolean includeEntityVariable = false;
    private boolean includeCapturedVariable = false;

    @Setter
    private boolean ownerEntityVariable = false;
    @Setter
    private boolean ownerCapturedVariable = false;

    public MethodStack(Object owner, MethodSignature signature, Object[] args) {
        this.owner = owner;
        boolean includeEntityVariable = owner instanceof EntityVariable;
        for(Object arg : args) {
            if (arg instanceof MethodStack methodStack && methodStack.includeEntityVariable()) {
                methodStack.parent = this;
                includeEntityVariable = true;
            }
        }
        if (includeEntityVariable) {
            this.includeEntityVariable(true);
        }

        this.signature = signature;
        this.args = args;
    }

    public void addStack(MethodStack stack) {
//        boolean includeEntityVariable = false;
//        for(Object arg : stack.args()) {
//            if (arg instanceof MethodStack methodStack && methodStack.includeEntityVariable()) {
//                methodStack.parent = this;
//                includeEntityVariable = true;
//            }
//        }
        if (stack.includeEntityVariable()) {
            stack.includeEntityVariable(true);
        }
        stacks.add(stack);
    }

    public void includeEntityVariable(boolean isEntity) {
        this.includeEntityVariable = isEntity;
        if(isEntity) {
            while (parent != null) {
                parent = parent.parent;
                parent.includeEntityVariable = true;
            }
        }
    }

    public void includeCapturedVariable(boolean isEntity) {
        this.includeEntityVariable = isEntity;
        if (parent != null) {
            parent.includeCapturedVariable = isEntity;
        }
    }
}
