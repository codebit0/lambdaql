package org.lambdaql.query.lambda;

import java.lang.reflect.Type;

public interface ICapturedValue {
    /**
     * captured value의 타입
     */
    Type type();

    /**
     * captured value의 값
     */
    Object value();

    /**
     * captured value의 순차 인덱스
     */
    int sequenceIndex();

    /**
     * lambda argument 시작 인덱스
     */
    int opcodeIndex();
}
