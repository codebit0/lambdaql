package org.lambdaql.analyzer;

/**
 * 람다 표현식에서 사용되는 엔티티 인자 분석 결과 클래스
 * @param type 인자 클래스 타입
 * @param value 클래스의 정규화된 이름 (예: java.lang.String -> java/lang/String)
 * @param alias 엔티티 별칭
 * @param sequenceIndex 호출 순서에 따른 인덱스 번호
 * @param opcodeIndex opcode 에서 지역변수 테이블을 참조하기 위한 인덱스 번호
 */
public record EntityVariable(Class<?> type, String typeSignature, String value, int sequenceIndex, int opcodeIndex) implements IColumn, ICapturedVariable {

    public EntityVariable(Class<?> type, String alias, int sequenceIndex, int opcodeIndex) {
        this(type, type.getCanonicalName().replaceAll("\\.", "/"), alias, sequenceIndex, opcodeIndex);
    }

    public String alias() {
        return value;
    }
}
