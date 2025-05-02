package org.lambdaql.analyzer;

import lombok.Getter;
import lombok.experimental.Accessors;

import static org.objectweb.asm.Opcodes.*;

public enum BinaryOperator {
    EQ("=", IFEQ, IF_ACMPEQ), NE("<>", IFNE, IF_ACMPNE), LT("<", IFLT),
    LE("<=", IFLE), GT(">", IFGT), GE(">=", IFGE),
    IS("IS", IF_ACMPEQ), IS_NOT("IS NOT", IF_ACMPNE),
    IN("IN", IF_ACMPEQ), NOT_IN("NOT IN", IF_ACMPNE),
    LIKE("LIKE", IF_ACMPEQ), NOT_LIKE("NOT LIKE", IF_ACMPNE),

    // 사칙연산자 추가 (int, long, double 타입 별로)
    ADD("+", IADD, LADD, DADD, FADD), SUB("-", ISUB, LSUB, DSUB, FSUB), MUL("*", IMUL, LMUL, DMUL, FMUL),
    DIV("/", IDIV, LDIV, DDIV, FDIV), MOD("%", IREM, LREM, DREM, FREM),

    // 비트 연산자 추가 (int, long 타입 별로)
    AND("&", IAND, LAND), OR("|", IOR, LOR), XOR("^", IXOR, LXOR),

    // 시프트 연산자 추가 (int, long 타입 별로)
    SHIFT_L("<<", ISHL, LSHL), SHIFT_R(">>", ISHR, LSHR), UNSIGNED_SHIFT(">>>", IUSHR, LUSHR);

    @Getter
    @Accessors(fluent = true)
    public final String symbol;


    public final int[] opcodes;  // 연산자에 해당하는 여러 opcode들을 배열로 처리

    // varargs를 사용하여 가변 인자 처리
    BinaryOperator(String symbol, int... opcodes) {
        this.symbol = symbol;
        this.opcodes = opcodes;
    }

    /**
     * opcode에 해당하는 BinaryOperator를 찾는 메소드
     * @param opcode opcode
     * @return enum BinaryOperator
     */
    public static BinaryOperator fromOpcode(int opcode) {
        for (BinaryOperator operator : BinaryOperator.values()) {
            for (int op : operator.opcodes) {
                if (op == opcode) {
                    return operator;
                }
            }
        }
        throw new UnsupportedOperationException("Unsupported opcode: " + opcode);
    }

    public static BinaryOperator fromSymbol(String symbol) {
        for (BinaryOperator b : BinaryOperator.values()) {
            if (b.symbol.equals(symbol)) {
                return b;
            }
        }
        throw new UnsupportedOperationException(symbol);
    }

    public BinaryOperator not() {
        return switch (this) {
            case EQ -> NE;
            case NE -> EQ;
            case LT -> GE;
            case LE -> GT;
            case GT -> LE;
            case GE -> LT;
            case IS -> IS_NOT;
            case IS_NOT -> IS;
            case IN -> NOT_IN;
            case NOT_IN -> IN;
            case LIKE -> NOT_LIKE;
            case NOT_LIKE -> LIKE;

            // 사칙연산자 반대
            case ADD -> SUB;
            case SUB -> ADD;
            case MUL -> DIV;
            case DIV -> MUL;
//            case MOD: return null; // 나머지 연산자의 반대는 정의하기 어려움

            // 비트 연산자 반대
            case AND -> OR;
            case OR -> AND;
            case XOR -> XOR; // XOR은 자기 자신이 반대 연산자임

            // 시프트 연산자는 반대 연산이 정의되지 않음
//            case SHIFT_L: return null; // 왼쪽 시프트에 대한 반대 연산은 정의되지 않음
//            case SHIFT_R: return null; // 오른쪽 시프트에 대한 반대 연산은 정의되지 않음
//            case UNSIGNED_SHIFT: return null; // 부호 없는 오른쪽 시프트에 대한 반대 연산은 정의되지 않음
            default -> throw new UnsupportedOperationException(this.toString());
        };
    }

    public String toString() {
        return name()+"("+symbol+")";
    }
}

