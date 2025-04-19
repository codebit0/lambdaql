package org.lambdaql.query.lambda;


import lombok.Getter;
import lombok.experimental.Accessors;

import java.lang.reflect.Type;

@Accessors(fluent = true)
@Getter
public class ConstantValue implements ICapturedValue , IOperand {
    private final Type type;
    private final Object value;
    private final int sequenceIndex;
    private final int opcodeIndex;
    /**
     * 람다 표현식에서 사용되는 상수값 분석 결과 클래스
     * @param type 인자 클래스 타입
     * @param value 클래스의 정규화된 이름 (예: java.lang.String -> java/lang/String)
     * @param sequenceIndex 호출 순서에 따른 인덱스 번호
     * @param opcodeIndex opcode 에서 지역변수 테이블을 참조하기 위한 인덱스 번호
     */

    public ConstantValue(Type type, Object value, int sequenceIndex, int opcodeIndex) {
        this.type = type;
        this.value = value;
        this.sequenceIndex = sequenceIndex;
        this.opcodeIndex = opcodeIndex;
    }
}
