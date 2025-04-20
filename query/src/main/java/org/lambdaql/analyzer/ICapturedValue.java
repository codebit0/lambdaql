package org.lambdaql.analyzer;

import java.lang.reflect.Type;

public interface ICapturedValue {
    /**
     * captured value의 타입
     */
    Type type();

    /**
     * captured value의 타입을 / 로 변환한 문자열로 변환
     */
    String typeSignature();

    /**
     * captured value의 값
     */
    Object value();

    /**
     * captured value의 순차 인덱스
     */
    int sequenceIndex();

    /**
     * analyzer argument 시작 인덱스
     */
    int opcodeIndex();
}
