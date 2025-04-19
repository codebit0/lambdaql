package org.lambdaql.query.lambda;

public record LambdaEntity(Class<?> type, Object value, String alias, int sequenceIndex, int opcodeIndex) implements IColumn, ICapturedValue {

    public LambdaEntity(Class<?> type, String alias, int sequenceIndex, int opcodeIndex) {
        this(type,  type.getCanonicalName().replaceAll("\\.", "/"), alias, sequenceIndex, opcodeIndex);
    }
}
