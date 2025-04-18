package org.lambdaql.query;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.lambdaql.query.lambda.LambdaVariable;
import org.objectweb.asm.*;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.temporal.TemporalAccessor;
import java.util.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.util.Printer.*;

public class LambdaPredicateVisitor extends MethodVisitor {

    private final Metamodel metamodel;
    private final List<Class<?>> entityClasses;
    private final SerializedLambda serializedLambda;

    private LambdaVariable lambdaVariable;

    private final Deque<Object> valueStack = new ArrayDeque<>();
    private final Deque<ConditionExpr> exprStack = new ArrayDeque<>();
    private final Deque<ConditionBlock> blockStack = new ArrayDeque<>();
    private ConditionExpr conditionExpr;

    private final ComparisonStateManager stateManager = new ComparisonStateManager();

    private class ConditionBlock {
        private final LogicalOperator operator;
        private final List<ConditionExpr> conditions = new ArrayList<>();
        private final List<Label> labels = new ArrayList<>();

        ConditionBlock(LogicalOperator operator) {
            this.operator = operator;
        }

        ConditionExpr toExpressionTree() {
            if (conditions.isEmpty()) return null;
            ConditionExpr result = conditions.get(0);
            for (int i = 1; i < conditions.size(); i++) {
                result = new LogicalCondition(operator, Arrays.asList(result, conditions.get(i)));
            }
            return result;
        }
    }

    private static final Set<String> DATE_TYPES = Set.of(
            "java/util/Date", "java/sql/Date", "java/sql/Time", "java/sql/Timestamp", "java/util/Calendar",
            "java/time/Instant", "java/time/LocalDate", "java/time/LocalTime", "java/time/LocalDateTime",
            "java/time/OffsetTime", "java/time/OffsetDateTime", "java/time/ZonedDateTime"
    );

    public LambdaPredicateVisitor(SerializedLambda serializedLambda, LambdaVariable lambdaVariable, Metamodel metamodel, int accessFlags) {
        super(ASM9);

        this.metamodel = metamodel;
        this.entityClasses = lambdaVariable.getEntityClasses();
        this.serializedLambda = serializedLambda;
        
        //FIXME 추후 제거, 런타임에 분석정보를 받도록 수정
        this.lambdaVariable = lambdaVariable;

    }



    /**
     * 메서드의 바이트코드 시작을 알립니다.
     * 보통 로컬 변수 설정 전에 호출됩니다.
     */
    @Override
    public void visitCode() {
        System.out.println("//visitCode");
        super.visitCode();
    }

    @Override
    public void visitFrame(
            final int type,
            final int numLocal,
            final Object[] local,
            final int numStack,
            final Object[] stack) {

        super.visitFrame(type, numLocal, local, numStack, stack);
        System.out.println("//visitFrame: type=" + type + ", numLocal=" + numLocal + ", local=" + Arrays.toString(local) +", numStack=" + numStack + ", stack=" + Arrays.toString(stack));
    }


    /**
     *
     * @param name 매개변수 이름 또는 제공되지 않은 경우 {@literal null}.
     * @param access 매개변수의 접근 플래그로, {@code ACC_FINAL}, {@code ACC_SYNTHETIC},
     *     또는/그리고 {@code ACC_MANDATED}만 허용됩니다 (참조: {@link Opcodes}).
     */
    @Override
    public void visitParameter(final String name, final int access) {
        super.visitParameter(name, access);
        System.out.println("visitParameter name:"+name + " access:"+access);
    }

    /**
     * Visits a non standard attribute of this method.
     * @param attribute an attribute.
     */
    @Override
    public void visitAttribute(org.objectweb.asm.Attribute attribute) {
        super.visitAttribute(attribute);
        System.out.println("visitAttribute: "+attribute);
    }

    /**
     * 로컬 변수 로딩 및 저장 (ILOAD, ISTORE, ALOAD, ASTORE 등)
     * @param opcode 로컬 변수 명령어의 opcode입니다. 이 opcode는 다음 중 하나일 수 있습니다:
     *     - ILOAD: int 타입 로컬 변수를 스택에 로드합니다.
     *     - LLOAD: long 타입 로컬 변수를 스택에 로드합니다.
     *     - FLOAD: float 타입 로컬 변수를 스택에 로드합니다.
     *     - DLOAD: double 타입 로컬 변수를 스택에 로드합니다.
     *     - ALOAD: 참조 타입 로컬 변수를 스택에 로드합니다.
     *     - ISTORE: 스택의 int 값을 로컬 변수에 저장합니다.
     *     - LSTORE: 스택의 long 값을 로컬 변수에 저장합니다.
     *     - FSTORE: 스택의 float 값을 로컬 변수에 저장합니다.
     *     - DSTORE: 스택의 double 값을 로컬 변수에 저장합니다.
     *     - ASTORE: 스택의 참조 값을 로컬 변수에 저장합니다.
     *     - RET: 특정 로컬 변수 인덱스에 저장된 jsr(jump to subroutine) 반환 주소로 복귀할 때 사용 (거의 사용 안 됨)
     * @param varIndex 명령어의 피연산자입니다. 이 피연산자는 로컬 변수의 인덱스입니다.
     */
    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        System.out.println("📦 visitVarInsn: opcode=" + opcode +" name:"+ OPCODES[opcode]+ ", varIndex=" + varIndex);
        System.out.println(" >> value "+findVarInsn(varIndex));
        switch (opcode) {
            case ALOAD, ILOAD, LLOAD, FLOAD, DLOAD -> {
                valueStack.push(findVarInsn(varIndex));
            }
            default -> {
                throw new UnsupportedOperationException("Unsupported opcode: " + OPCODES[opcode]);
            }

            /*case ALOAD -> {
                if ((capturedValues.isEmpty() && varIndex == 0)
                        || varIndex > capturedValues.size()) {
                    // 로컬 변수 인덱스가 캡쳐된 값보다 크면, 람다 선언부 타입 변수
                    // 예: Predicate<SomeEntity> predicate = e -> e.getId() == 1;
                    // 에서 e.getId() == 1 부분의 e
                    System.out.println("   ALOAD: lambda variable " + varIndex);
                    valueStack.push(new LambdaEntity(entityClasses.get(0)));
                }
            } case ILOAD, LLOAD, FLOAD, DLOAD  -> {
                if(capturedValues.containsKey(varIndex)){
                    // 캡쳐된 로컬 변수
                    CapturedValue capturedValue = capturedValues.get(varIndex);
                    Object value = capturedValue.value();
                    System.out.println(OPCODES[opcode]+" : captured value " + varIndex + " = " + value);
                    valueStack.push(value);
                } else {
                    // 로컬 변수 인덱스가 캡쳐된 값보다 크면, 람다 선언부 타입 변수
                    // 예: Predicate<SomeEntity> predicate = e -> e.getId() == 1;
                    // 에서 e.getId() == 1 부분의 e
                    System.out.println(OPCODES[opcode]+" : lambda variable load error" + varIndex);
                }
            }*/
        }
    }

    private Object findVarInsn(int varIndex) {
        return lambdaVariable.getCapturedValueOpcodeIndex(varIndex);
    }

    /**
     * 메서드 호출 (INVOKEVIRTUAL, INVOKESTATIC 등)
     *
     * Getter/Setter 추출, equals/contains 등 의미 기반 DSL 변환에 핵심.
     *
     * visitMethodInsn(...) 또는 visitFieldInsn(...) 으로 좌항 (예: o.getId()) push
     * visitLdcInsn(...) 또는 상수 처리로 우항 (예: 1) push
     * visitJumpInsn(...) → == 등 비교 수행
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        System.out.println("📡 visitMethodInsn: owner=" + owner + ", name=" + name + ", desc=" + descriptor);


        //primitive unboxing 메서드 자동 생성 문제 해결
        if (isPrimitiveUnboxingMethod(opcode, owner, name)) {
            if (!valueStack.isEmpty()) {
                Object boxed = valueStack.pop();
                valueStack.push(boxed); // 언박싱된 primitive 값을 푸시한다고 간주 (SQL 표현에는 영향 없음)
                System.out.println("   🔄 언박싱 처리: " + boxed);
            } else {
                System.err.println("⚠️ valueStack is empty during unboxing: " + owner + "." + name);
            }
            return;
        }

        /*if (!valueStack.isEmpty() && valueStack.peek() instanceof EmbeddedContext embeddedContext) {
            valueStack.pop();
            String column = resolveColumnNameRecursive(
                    embeddedContext.embeddedClass,
                    name.startsWith("get") ? Character.toLowerCase(name.charAt(3)) + name.substring(4) : name,
                    embeddedContext.prefix
            );
            if (column != null) valueStack.push(column);
            return;
        }*/

        String resolvedLeft = getFieldFromMethodName(owner, name);
        if (resolvedLeft != null) {
            valueStack.push(resolvedLeft);
            return;
        }

        if (DATE_TYPES.contains(owner)) {
            Object right = valueStack.pop();
            Object left = valueStack.pop();
            switch (name) {
                case "equals" -> pushBinaryExpr(left, BinaryOperator.EQ, right);
                case "before" -> pushBinaryExpr(left, BinaryOperator.LT, right);
                case "after" -> pushBinaryExpr(left, BinaryOperator.GT, right);
            }
        } else {
            switch (name) {
                case "equals" -> {
                    Object right = valueStack.pop();
                    Object left = valueStack.pop();
                    BinaryOperator op = right == null ? BinaryOperator.IS : BinaryOperator.EQ;
                    pushBinaryExpr(left, op, right);
                }
                case "contains" -> pushBinaryExpr(valueStack.pop(), BinaryOperator.IN, valueStack.pop());
                case "matches" -> pushBinaryExpr(valueStack.pop(), BinaryOperator.LIKE, valueStack.pop());
                case "between" -> {
                    Object to = valueStack.pop();
                    Object from = valueStack.pop();
                    Object field = valueStack.pop();
                    pushLogicalExpr(LogicalOperator.AND,
                            new BinaryCondition(field.toString(), BinaryOperator.GE.symbol, from),
                            new BinaryCondition(field.toString(), BinaryOperator.LE.symbol, to));
                }
                case "startsWith" -> {
                    Object prefix = valueStack.pop();
                    Object field = valueStack.pop();
                    pushBinaryExpr(field, BinaryOperator.LIKE, prefix + "%");
                }
                case "endsWith" -> {
                    Object suffix = valueStack.pop();
                    Object field = valueStack.pop();
                    pushBinaryExpr(field, BinaryOperator.LIKE, "%" + suffix);
                }
            }
        }
    }

    /**
     * 객체 필드 접근 (GETFIELD, PUTFIELD, GETSTATIC, 등)
     * owner가 엔티티 클래스인지 확인 후, 컬럼 이름으로 해석.
     *
     * @param opcode the opcode of the type instruction to be visited. This opcode is either
     *     GETSTATIC, PUTSTATIC, GETFIELD or PUTFIELD.
     * @param owner the internal name of the field's owner class (see {@link Type#getInternalName()}).
     * @param name the field's name.
     * @param descriptor the field's descriptor (see {@link Type}).
     */
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        System.out.println("🏷 FieldInsn: " + owner + "." + name + " " + descriptor);
        if (opcode == GETFIELD) {
            valueStack.push(resolveColumnNameRecursive(entityClasses.get(0), name, ""));
        }
    }

    /**
     * 상수값 로딩을 처리합니다.
     * LDC 상수를 값으로 변환하여 스택에 푸시합니다.
     * @param cst the constant to be loaded on the stack. This parameter must be a non null {@link
     *     Integer}, a {@link Float}, a {@link Long}, a {@link Double}, a {@link String}, a {@link
     *     Type} of OBJECT or ARRAY sort for {@code .class} constants, for classes whose version is
     *     49, a {@link Type} of METHOD sort for MethodType, a {@link Handle} for MethodHandle
     *     constants, for classes whose version is 51 or a {@link ConstantDynamic} for a constant
     *     dynamic for classes whose version is 55.
     */
    @Override
    public void visitLdcInsn(Object cst) {
        System.out.println("💾 visitLdcInsn LDC: " + cst);
        if (cst instanceof Date date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            valueStack.push("'" + sdf.format(date) + "'");
        } else if (cst instanceof TemporalAccessor time) {
            valueStack.push("'" + time.toString().replace("T", " ") + "'");
        } else {
            valueStack.push(cst);
        }
    }

    /**
     * 일반 명령 (IRETURN, IADD, ICONST_1, 등)
     * 스택 기반 연산 및 return 등 의미 해석.
     * DSL에서는 exprStack, valueStack 조작에 사용됨.
     *
     * @param opcode the opcode of the instruction to be visited. This opcode is either NOP,
     *     ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5,
     *     LCONST_0, LCONST_1, FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1, IALOAD, LALOAD,
     *     FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD, IASTORE, LASTORE, FASTORE, DASTORE,
     *     AASTORE, BASTORE, CASTORE, SASTORE, POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2,
     *     SWAP, IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL, DMUL, IDIV, LDIV,
     *     FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG, FNEG, DNEG, ISHL, LSHL, ISHR, LSHR, IUSHR,
     *     LUSHR, IAND, LAND, IOR, LOR, IXOR, LXOR, I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I,
     *     D2L, D2F, I2B, I2C, I2S, LCMP, FCMPL, FCMPG, DCMPL, DCMPG, IRETURN, LRETURN, FRETURN,
     *     DRETURN, ARETURN, RETURN, ARRAYLENGTH, ATHROW, MONITORENTER, or MONITOREXIT.
     */
    @Override
    public void visitInsn(int opcode) {
        System.out.println("//visitInsn:"+ OPCODES[opcode] + " opcode=" + opcode);
        switch (opcode) {
            //상수값으로도 쓰이지만 lcmp류의 반환값으로도 사용됨
            case ICONST_0 -> {
                System.out.println("🧱 ICONST_0 → push 0");
                if (stateManager.hasPendingComparison()) {
                    stateManager.setExpectedResult(false);
                } else {
                    valueStack.push(0);
                }
            }
            case ICONST_1 -> {
                System.out.println("🧱 ICONST_1 → push 1");
                if (stateManager.hasPendingComparison()) {
                    stateManager.setExpectedResult(true);
                } else {
                    valueStack.push(1);
                }
            }
            case ICONST_2, ICONST_3, ICONST_4, ICONST_5 -> {
                valueStack.push(opcode -3);
                System.out.println("🧱 "+ OPCODES[opcode] +" → push "+(opcode -3));
            }
            case LCONST_0 -> {
                System.out.println("🧱 LCONST_0 → push 0L");
                valueStack.push(0L);
            }
            case LCONST_1 -> {
                System.out.println("🧱 LCONST_1 → push 1L");
                valueStack.push(1L);
            }
            case IADD, ISUB, IMUL, IDIV -> {
                Object right = valueStack.pop();
                Object left = valueStack.pop();
                String op = switch (opcode) {
                    case IADD -> "+";
                    case ISUB -> "-";
                    case IMUL -> "*";
                    case IDIV -> "/";
                    default -> throw new IllegalStateException("Unexpected value: " + opcode);
                };
                valueStack.push("(" + left + " " + op + " " + right + ")");
            }
            case IAND -> {
                pushLogicalExpr(LogicalOperator.AND, exprStack.pop(), exprStack.pop());
            }
            case IOR -> {
                pushLogicalExpr(LogicalOperator.OR, exprStack.pop(), exprStack.pop());
            }
            case LCMP -> {
                //LCMP는 항상 비교 조건으로 변환되어야 하므로, 조건 분기 없이 쓰이는 LCMP는 분석 대상에서 제외
                //두 개의 long 값을 비교해서, 결과를 int로 푸시하는 비교 전용 명령어로 같으면 0, 왼쪽이 크면 1, 오른쪽이 크면 -1을 푸시합니다.
                if (valueStack.size() < 2) {
                    //값비교를 위해서는 항상 2개의 변수가 필요
                    System.err.println("❌ LCMP: insufficient operands, stack=" + valueStack);
                    return;
                }
                Object right = valueStack.pop();
                Object left = valueStack.pop();
                //값 비교는 0,1,-1 을 반환하므로 IFXX lable 이 따라옴
                stateManager.captureComparison(left, right);
                System.out.println("🧮 LCMP → push ComparisonResult(" + left + ", " + right + ")");
            }
//            case ICONST_0, ICONST_1 -> valueStack.push(opcode == ICONST_1);
            case IRETURN,ARETURN -> {
                if (stateManager.hasPendingComparison()) {
                    ComparisonResult cr = stateManager.consumeComparison();
                    BinaryOperator op = stateManager.resolveFinalOperator();
                    pushBinaryExpr(cr.left(), op, cr.right());
                }
//                if (exprStack.isEmpty()) {
//                    System.err.println("❌ exprStack is empty at return");
//                    conditionExpr = null;
//                } else {
//                    conditionExpr = exprStack.pop();
//                    System.out.println("✅ 최종 조건 expr 설정됨: " + conditionExpr);
//                }
//                if (!blockStack.isEmpty()) {
//                    conditionExpr = blockStack.pop().toExpressionTree();
//                } else {
//                    conditionExpr = buildExpressionTree();
//                }
            }
            //case IFNE, IFEQ -> pushLogicalExpr(LogicalOperator.NOT, exprStack.pop());
            default -> {
                System.out.println("ℹ️ visitInsn: opcode=" + opcode);
            }
        }
    }

    /**
     * 조건 분기 및 점프 (IFEQ, IF_ICMPEQ, GOTO 등)
     * 비교 연산자 기반 BinaryCondition 생성에 사용.
     * 예: IF_ICMPGT → > 연산 해석.
     *
     * @param opcode 명령어의 opcode입니다. 이 opcode는 다음 중 하나일 수 있습니다:
     *     - IFEQ: 스택의 값이 0인지 비교하여 조건 분기.
     *     - IFNE: 스택의 값이 0이 아닌지 비교하여 조건 분기.
     *     - IFLT: 스택의 값이 0보다 작은지 비교하여 조건 분기.
     *     - IFGE: 스택의 값이 0보다 크거나 같은지 비교하여 조건 분기.
     *     - IFGT: 스택의 값이 0보다 큰지 비교하여 조건 분기.
     *     - IFLE: 스택의 값이 0보다 작거나 같은지 비교하여 조건 분기.
     *     - IF_ICMPEQ: 두 int 값이 같은지 비교하여 조건 분기.
     *     - IF_ICMPNE: 두 int 값이 다른지 비교하여 조건 분기.
     *     - IF_ICMPLT: 두 int 값 중 왼쪽 값이 작은지 비교하여 조건 분기.
     *     - IF_ICMPGE: 두 int 값 중 왼쪽 값이 크거나 같은지 비교하여 조건 분기.
     *     - IF_ICMPGT: 두 int 값 중 왼쪽 값이 큰지 비교하여 조건 분기.
     *     - IF_ICMPLE: 두 int 값 중 왼쪽 값이 작거나 같은지 비교하여 조건 분기.
     *     - IF_ACMPEQ: 두 참조형 객체가 같은지 비교하여 조건 분기.
     *     - IF_ACMPNE: 두 참조형 객체가 다른지 비교하여 조건 분기.
     *     - GOTO: 무조건 지정된 라벨로 점프.
     *     - JSR: 서브루틴 호출 (거의 사용되지 않음).
     *     - IFNULL: 스택의 값이 null인지 비교하여 조건 분기.
     *     - IFNONNULL: 스택의 값이 null이 아닌지 비교하여 조건 분기.
     * @param label 명령어의 피연산자로, 점프 명령이 이동할 위치를 나타내는 라벨입니다.
     */
    @Override
    public void visitJumpInsn(int opcode, Label label) {
        System.out.println("//visitJumpInsn:" + OPCODES[opcode]+ " label=" + label);
        switch (opcode) {
            case IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPLE, IF_ICMPGT, IF_ICMPGE -> {
                //int, boolean, byte, char, short 는 내부적으로 모두 int로 변환되기 때문에
                //별도 ICMP는 없고 바로 IF_ICMPxx 조건 분기 명령으로 처리
                if (valueStack.size() < 2) {
                    //비교 구문이므로 두개의 stack이 필요
                    System.err.println("❌ Stack too small at jump opcode: " + opcode);
                    new RuntimeException().printStackTrace();
                    return;
                }

                Object right = valueStack.pop();
                Object left = valueStack.pop();

                BinaryOperator operator = switch (opcode) {
                    case IF_ICMPEQ -> BinaryOperator.EQ;
                    case IF_ICMPNE -> BinaryOperator.NE;
                    case IF_ICMPLT -> BinaryOperator.LT;
                    case IF_ICMPLE -> BinaryOperator.LE;
                    case IF_ICMPGT -> BinaryOperator.GT;
                    case IF_ICMPGE -> BinaryOperator.GE;
                    default -> throw new UnsupportedOperationException("Unsupported jump opcode: " + opcode);
                };

                pushBinaryExpr(left, operator, right);
                System.out.println("✅ 비교 조건 추가됨: " + left + " " + operator.symbol + " " + right);
            }
            case IFEQ, IFNE, IFLT, IFLE, IFGT, IFGE -> {
                //IFGT > , IFGE >=, IFLT <, IFLE <=, IFNE !=, IFEQ ==
                System.out.println("🔍 "+ OPCODES[opcode] +" detected: opcode = " + opcode + ", label = "+ label + ", stack = " + valueStack);
                if (stateManager.hasPendingComparison()) {
//                    BinaryOperator op = stateManager.resolveOperatorForOpcode(opcode);
//                    ComparisonResult cr = stateManager.consumeComparison();
//                    ConditionExpr expr = new BinaryCondition(cr.left().toString(), op.symbol, cr.right());
//                    exprStack.push(expr);
                    stateManager.registerBranch(opcode, label);
                }
                //stateManager.registerBranch(opcode, label);
            }
            case IF_ACMPEQ -> {
                //TODO 참조형 레퍼런스 주소 비교를 id비교로 변경
            }
            case GOTO -> {
                System.out.println("🔁 GOTO encountered: jump to label " + label);
            }

            default -> {
                throw new UnsupportedOperationException("Unsupported jump opcode: " + opcode);
            }
        }
    }



    /**
     * 예외 처리 블록 설정
     * @param start the beginning of the exception handler's scope (inclusive).
     * @param end the end of the exception handler's scope (exclusive).
     * @param handler the beginning of the exception handler's code.
     * @param type the internal name of the type of exceptions handled by the handler (see {@link
     *     Type#getInternalName()}), or {@literal null} to catch any exceptions (for "finally"
     *     blocks).
     */
    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        System.out.println("🔄 visitTryCatchBlock: start=" + start + ", end=" + end + ", handler=" + handler + ", type=" + type);
        super.visitTryCatchBlock(start, end, handler, type);
    }

    /**
     * 로컬 변수 테이블을 방문합니다.
     * @param name the name of a local variable.
     * @param desc the type descriptor of this local variable.
     * @param signature the type signature of this local variable. May be {@literal null} if the local
     *     variable type does not use generic types.
     * @param start the first instruction corresponding to the scope of this local variable
     *     (inclusive).
     * @param end the last instruction corresponding to the scope of this local variable (exclusive).
     * @param index the local variable's index.
     */
    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        System.out.println("📦 visitLocalVariable: name=" + name + ", desc=" + desc + ", signature=" + signature + ", start=" + start + ", end=" + end + ", index=" + index);
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    /**
     * 코드 내 특정 위치를 식별하는 라벨.
     * @param label a {@link Label} object.
     */
    @Override
    public void visitLabel(Label label) {
        System.out.println("🏷 visitLabel: " + label);
        stateManager.setCurrentLabel(label);
        super.visitLabel(label);
    }

    /**
     * 바이트코드 끝을 알립니다.
     * 최종적으로 조건 표현식 등을 반환하거나 정리하는 데 사용됩니다.
     * conditionExpr 마무리 작업
     */
    @Override
    public void visitEnd() {
        if (conditionExpr == null && !exprStack.isEmpty()) {
            conditionExpr = exprStack.pop();
        }
        super.visitEnd();
    }

    public ConditionExpr getConditionExpr() {
        if (exprStack.isEmpty()) return null;
        List<ConditionExpr> all = new ArrayList<>(exprStack);
        exprStack.clear();

        // 분기된 OR 조건이 포함되었는지 상태로 판단할 수 있다면 여기서 구분 처리 필요
        LogicalOperator operator = all.stream().anyMatch(expr -> expr instanceof LogicalCondition lc && lc.operator == LogicalOperator.OR)
                ? LogicalOperator.OR
                : LogicalOperator.AND;

        return all.size() == 1
                ? all.get(0)
                : new LogicalCondition(operator, all);
//        return conditionExpr;
    }

    private String resolveColumnNameRecursive(Class<?> currentClass, String fieldName, String prefix) {
        try {
            EntityType<?> entityType = metamodel.entity(currentClass);
            Attribute<?, ?> attr = entityType.getAttribute(fieldName);

            Member member = attr.getJavaMember();

            if (member instanceof Method method) {
                Column column = method.getAnnotation(Column.class);
                if (column != null && !column.name().isEmpty()) {
                    return prefix + column.name();
                }
                String name = method.getName();
                if (name.startsWith("get") && name.length() > 3) {
                    return prefix + Character.toLowerCase(name.charAt(3)) + name.substring(4);
                } else if (name.startsWith("is") && name.length() > 2) {
                    return prefix + Character.toLowerCase(name.charAt(2)) + name.substring(3);
                }
            } else if (member instanceof Field field) {
                if (field.isAnnotationPresent(Embedded.class)) {
                    Class<?> embeddedType = field.getType();
                    valueStack.push(new EmbeddedContext(embeddedType, prefix + fieldName + "."));
                    return null;
                }
                Column column = field.getAnnotation(Column.class);
                if (column != null && !column.name().isEmpty()) {
                    return prefix + column.name();
                }
                return prefix + field.getName();
            }
            return prefix + attr.getName();
        } catch (IllegalArgumentException e) {
            return prefix + fieldName;
        }
    }

    private String resolveColumnNameFromGetter(String owner, String methodName) {
//        if (!owner.replace("/", ".").equals(entityClass.getName())) {
//            return null;
//        }

        String fieldName = null;
        if (methodName.startsWith("get") && methodName.length() > 3) {
            fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            fieldName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        }
//        if (fieldName != null) {
//            return resolveColumnNameRecursive(entityClass, fieldName, "");
//        }
        return null;
    }

    private void pushBinaryExpr(Object left, BinaryOperator op, Object right) {
        exprStack.push(new BinaryCondition(left.toString(), op.symbol, right));
    }

    private void pushLogicalExpr(LogicalOperator op, ConditionExpr... exprs) {
        exprStack.push(new LogicalCondition(op, Arrays.asList(exprs)));
    }

    private ConditionExpr buildExpressionTree() {
        if (exprStack.isEmpty()) return null;
        List<ConditionExpr> exprs = new ArrayList<>();
        while (!exprStack.isEmpty()) exprs.add(exprStack.pop());
        Collections.reverse(exprs);
        if (exprs.size() == 1) return exprs.get(0);
        return new LogicalCondition(LogicalOperator.AND, exprs);
    }

    private String getFieldFromMethodName(String owner, String methodName) {
//        if (!owner.replace("/", ".").equals(entityClass.getName())) return null;

        String fieldName = null;
        if (methodName.startsWith("get") && methodName.length() > 3)
            fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        else if (methodName.startsWith("is") && methodName.length() > 2)
            fieldName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);

        return fieldName != null ? resolveColumnNameRecursive(entityClasses.get(0), fieldName, "") : null;
    }

    private boolean isPrimitiveUnboxingMethod(int opcode, String owner, String name) {
        if(opcode != INVOKEVIRTUAL) return false;
        return switch (owner) {
            case "java/lang/Integer" -> name.equals("intValue");
            case "java/lang/Long"    -> name.equals("longValue");
            case "java/lang/Double"  -> name.equals("doubleValue");
            case "java/lang/Float"   -> name.equals("floatValue");
            case "java/lang/Short"   -> name.equals("shortValue");
            case "java/lang/Byte"    -> name.equals("byteValue");
            case "java/lang/Boolean" -> name.equals("booleanValue");
            case "java/lang/Character" -> name.equals("charValue");
            default -> false;
        };
    }

    static class EmbeddedContext {
        final Class<?> embeddedClass;
        final String prefix;

        EmbeddedContext(Class<?> embeddedClass, String prefix) {
            this.embeddedClass = embeddedClass;
            this.prefix = prefix;
        }
    }
}