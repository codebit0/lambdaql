package org.lambdaql.analyzer;

import org.lambdaql.utils.PrimitiveHelper;

/**
 * Represents a captured variable in a lambda expression.
 * @param type the type of the captured variable
 * @param typeSignature . 문자를 /로 바꾼 타입 시그니처
 * @param value 실제값
 * @param sequenceIndex 내부 저장테이블에 저장되는 인덱스
 * @param opcodeIndex 바이트코드에서 호출 될때 사용되는 인덱스
 */
public record ObjectCapturedVariable(Class<?> type, String typeSignature, Object value, int sequenceIndex, int opcodeIndex) implements ICapturedVariable {
    public ObjectCapturedVariable(Class<?> type, Object value, int sequenceIndex, int opcodeIndex) {
        this(type, type.getCanonicalName().replaceAll("\\.", "/"), value, sequenceIndex, opcodeIndex);
    }

    public boolean isBoolean() {
        return PrimitiveHelper.isBoolean(type);
    }

    public boolean isInt() {
        return PrimitiveHelper.isInt(type);
    }
}
