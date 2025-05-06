package org.lambdaql.analyzer;

import lombok.Getter;
import lombok.experimental.Accessors;

import static org.objectweb.asm.Opcodes.*;

public enum UnaryOperator {
    //0(false) 과 같으면 이란 뜻이므로
    FALSE("false", IFEQ),
    //0이 아니면(true) 이란 뜻이므로
    TRUE("true", IFNE);

    @Getter
    @Accessors(fluent = true)
    public final String symbol;


    public final int[] opcodes;  // 연산자에 해당하는 여러 opcode들을 배열로 처리

    // varargs를 사용하여 가변 인자 처리
    UnaryOperator(String symbol, int... opcodes) {
        this.symbol = symbol;
        this.opcodes = opcodes;
    }

    /**
     * opcode에 해당하는 BinaryOperator를 찾는 메소드
     * @param opcode opcode
     * @return enum BinaryOperator
     */
    public static UnaryOperator fromOpcode(int opcode) {
        for (UnaryOperator operator : UnaryOperator.values()) {
            for (int op : operator.opcodes) {
                if (op == opcode) {
                    return operator;
                }
            }
        }
        throw new UnsupportedOperationException("Unsupported opcode: " + opcode);
    }

    public static UnaryOperator fromSymbol(String symbol) {
        for (UnaryOperator b : UnaryOperator.values()) {
            if (b.symbol.equals(symbol)) {
                return b;
            }
        }
        throw new UnsupportedOperationException(symbol);
    }

    public UnaryOperator not() {
        return switch (this) {
            case FALSE -> TRUE;
            case TRUE -> FALSE;
            default -> throw new UnsupportedOperationException(this.toString());
        };
    }

    public String toString() {
        return name()+"("+symbol+")";
    }
}

