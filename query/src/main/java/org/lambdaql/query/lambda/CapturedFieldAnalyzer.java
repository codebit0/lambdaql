package org.lambdaql.query.lambda;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ASM9;


public class CapturedFieldAnalyzer  extends ClassVisitor {

    private final Map<String, String> capturedFieldDesc = new HashMap<>();

    public CapturedFieldAnalyzer() {
        super(ASM9);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if ((access & ACC_SYNTHETIC) != 0) {
            capturedFieldDesc.put(name, descriptor);
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    public Map<String, String> getCapturedFieldDesc() {
        return capturedFieldDesc;
    }
}
