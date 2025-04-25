package org.lambdaql.analyzer;


public record ObjectCapturedVariable(Class<?> type, String typeSignature, Object value, int sequenceIndex, int opcodeIndex) implements ICapturedVariable {
    public ObjectCapturedVariable(Class<?> type, Object value, int sequenceIndex, int opcodeIndex) {
        this(type, type.getCanonicalName().replaceAll("\\.", "/"), value, sequenceIndex, opcodeIndex);
    }
}
