package org.lambdaql.analyzer;


public record ObjectCapturedValue(Class<?> type, String typeSignature, Object value, int sequenceIndex, int opcodeIndex) implements ICapturedValue {
    public ObjectCapturedValue(Class<?> type, Object value, int sequenceIndex, int opcodeIndex) {
        this(type, type.getCanonicalName().replaceAll("\\.", "/"), value, sequenceIndex, opcodeIndex);
    }
}
