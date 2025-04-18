package org.lambdaql.query.lambda;

import lombok.Getter;
import org.lambdaql.query.CapturedValue;
import org.objectweb.asm.Opcodes;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LambdaVariable {

    /**
     * captured value의 순차 인덱스
     */
    private final Map<Integer, CapturedValue> capturedValues =new HashMap<>();

    @Getter
    private final List<Class<?>> entityClasses;

    @Getter
    private final   boolean isStatic;

    /**
     * lambda argument 시작 인덱스
     */
    @Getter
    private final int lambdaArgumentStartIndex;

    public LambdaVariable(Method method, SerializedLambda serializedLambda, List<Class<?>> entityClasses, int accessFlags) {
        this.entityClasses = entityClasses;
        isStatic = (accessFlags & Opcodes.ACC_STATIC) != 0;

        String capturingClass = serializedLambda.getCapturingClass();
        System.out.println("capturingClass: " + capturingClass);
        //captured의 타입으로는 필드의 타입을 알 수 없으므로 필드 정보를 얻어옴
        Field[] fields = method.getDeclaringClass().getDeclaredFields();

        int nextIndex = 0;
        for (int index = 0; index < serializedLambda.getCapturedArgCount(); index++) {
            Object captured = serializedLambda.getCapturedArg(index);
            Field field = fields[index];
            
            System.out.println("Captured value: " + captured+ " index: " + index+ " varIndex "+nextIndex+ " field: " + field);

            /*if(index == 0 && !isStatic) {
                //첫번째 인자에 호출측 this가 들어감
                CapturedValue capturedValue = new CapturedValue(capturingClass, captured, index, nextIndex);
                capturedValues.put(0, capturedValue);
                nextIndex++;
                continue;
            }*/

            Class<?> type = field.getType();
            CapturedValue capturedValue = new CapturedValue(type, captured, index, nextIndex);
            capturedValues.put(index, capturedValue);

            if(type == long.class || type == double.class) {
                nextIndex +=2;
            } else {
                nextIndex++;
            }
        }
        this.lambdaArgumentStartIndex = nextIndex;

        int index = capturedValues.size();
        int aliasIndex = 0;
        for(Class<?> entityClass : entityClasses) {
            System.out.println("Captured Entity: index: " + index+ " varIndex "+nextIndex+ " type: " + entityClass);
            LambdaEntity entity = new LambdaEntity(entityClass, entityClass.getSimpleName().substring(0, 2).toLowerCase() + aliasIndex++);
            CapturedValue capturedValue = new CapturedValue(LambdaEntity.class, entity, index, nextIndex++);
            capturedValues.put(index, capturedValue);
            index++;
        }
    }

    public CapturedValue getCapturedValue(int index) {
        return capturedValues.get(index);
    }

    public CapturedValue getCapturedValueOpcodeIndex(int opcodeIndex) {
        return capturedValues.get(toCapturedIndex(opcodeIndex));
    }

    public int size() {
        return capturedValues.size();
    }

    /**
     * 분석 시점에 예상되는 인덱스 번호 반환
     * @param index 파라미터 호출 순번
     * @return opcode 인덱스 번호
     */
    public int toOpcodeIndex(int index) {
        CapturedValue capturedValue = capturedValues.get(index);
        if(capturedValue != null) {
            return capturedValue.opcodeIndex();
        }
        throw new IllegalArgumentException("Invalid index:" + index);
    }

    public int toCapturedIndex(int opcodeIndex) {
        for(CapturedValue capturedValue : capturedValues.values()) {
            if(capturedValue.opcodeIndex() == opcodeIndex) {
                return capturedValue.capturedIndex();
            }
        }
        throw new IllegalArgumentException("Invalid opcode index:" + opcodeIndex);
    }

    /**
     * 첫 실행 후 캐시된 값을 runtime에 맞게 조정한다.
     * 첫 싫행 시점에 lambdaVariable의 인덱스를 분석한 후 인덱스를 지정하고 캐시하지만 런타임 시에 인덱스테이블이 형변환 되면 틀어짐
     * 이런 경우에 런타임 시점에 인덱스를 조정한다.
     * @param lambdaVariable 런타임에 분석된 lambdaVariable
     */
    public void runtimeAdjustIndex(LambdaVariable lambdaVariable) {

    }
}
