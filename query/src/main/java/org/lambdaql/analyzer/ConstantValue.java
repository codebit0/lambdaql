package org.lambdaql.analyzer;

/**
 * 상수값을 저장하기 위한 클래스
 * @param value 상수값
 */
public record ConstantValue(Object value) implements IOperand, IValue {

    public static ConstantValue of(Object value) {
        return new ConstantValue(value);
    }
}
