package org.lambdaql.analyzer;

import org.lambdaql.analyzer.label.Goto;
import org.lambdaql.analyzer.label.LabelInfo;
import org.lambdaql.analyzer.label.Return;
import org.lambdaql.analyzer.node.ConditionGroupNode;
import org.lambdaql.analyzer.node.ConditionLeafNode;
import org.lambdaql.analyzer.node.ConditionNode;
import org.lambdaql.query.QueryBuilder;
import org.objectweb.asm.*;

import java.lang.invoke.SerializedLambda;
import java.util.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.util.Printer.*;

public class LambdaPredicateVisitor extends MethodVisitor {

    private final QueryBuilder queryBuilder;
    private final List<Class<?>> entityClasses;
    private final SerializedLambda serializedLambda;

    private LambdaVariableAnalyzer lambdaVariable;

    private final Deque<Object> valueStack = new ArrayDeque<>();
    private final List<ConditionExpression> exprStack = new ArrayList<>();
    private final Map<Label, LabelInfo> labels = new HashMap<>();
//    private final MultiValueMap<Label, BinaryCondition> labelNConditions = new MultiValueMap<Label,BinaryCondition>();


//    private static final Set<String> DATE_TYPES = Set.of(
//            "java/util/Date", "java/sql/Date", "java/sql/Time", "java/sql/Timestamp", "java/util/Calendar",
//            "java/time/Instant", "java/time/LocalDate", "java/time/LocalTime", "java/time/LocalDateTime",
//            "java/time/OffsetTime", "java/time/OffsetDateTime", "java/time/ZonedDateTime"
//    );

    public LambdaPredicateVisitor(QueryBuilder queryBuilder, SerializedLambda serializedLambda, LambdaVariableAnalyzer lambdaVariable, int accessFlags) {
        super(ASM9);

        this.queryBuilder = queryBuilder;
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
        System.out.println("//visitCode start init");
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
        System.out.println("//visitVarInsn: opcode=" + opcode +" name:"+ OPCODES[opcode]+ ", varIndex=" + varIndex);
        System.out.println("📦 visitVarInsn: opcode=" + opcode +" name:"+ OPCODES[opcode]+ ", varIndex=" + varIndex+ " >> valueStack push value "+ findCaptureVarInsn(varIndex));

        switch (opcode) {
            case ALOAD, ILOAD, LLOAD, FLOAD, DLOAD -> {
                valueStack.push(findCaptureVarInsn(varIndex));
            }
            default -> {
                throw new UnsupportedOperationException("Unsupported opcode: " + OPCODES[opcode]);
            }
        }
    }

    private ICapturedVariable findCaptureVarInsn(int varIndex) {
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
        //TODO 추후 분리
        if (isPrimitiveUnboxingMethod(opcode, owner, name)) {
            if (!valueStack.isEmpty()) {
//                Object boxed = valueStack.pop();
//                valueStack.push(boxed); // 언박싱된 primitive 값을 푸시한다고 간주 (SQL 표현에는 영향 없음)
                Object boxed = valueStack.peek();
                System.out.println("   🔄 언박싱 처리: " + boxed);
            } else {
                System.err.println("⚠️ valueStack is empty during unboxing: " + owner + "." + name);
            }
            return;
        }
        MethodSignature methodSignature = MethodSignature.parse(owner, name, descriptor, isInterface);
        boolean isStatic = methodSignature.isStatic();
        int paramCount = methodSignature.method().getParameterCount();
        Object[] params = new Object[paramCount];
        boolean isEntity = false;

        Object peek = valueStack.peek();
        MethodStack beforeStack = null;
        if (peek instanceof MethodStack) {
            // 중간 스택 처리
            beforeStack = (MethodStack) valueStack.pop();
        }

        // 파라미터 역순으로 스택에서 꺼내기
        for (int i = paramCount - 1; i >= 0; i--) {
            Object value = valueStack.pop();
            if (value instanceof EntityVariable) {
                isEntity = true;
            }
            params[i] = value;
        }

        // static 이 아니면 인스턴스 객체도 꺼내야 함
        Object instance = null;
        if (!isStatic) {
            Object o = valueStack.pop();
            if (o instanceof EntityVariable) {
                isEntity = true;
            }
            instance = o;
        }

        MethodStack methodStack = new MethodStack(instance, methodSignature, params);
        methodStack.entity(isEntity);

        if (beforeStack != null) {
            beforeStack.entity(isEntity);
            beforeStack.addStack(methodStack);
            valueStack.push(methodStack);
        } else {
            valueStack.push(methodStack);
        }


        /*if (DATE_TYPES.contains(owner)) {
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
        }*/
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
            //valueStack.push(resolveColumnNameRecursive(entityClasses.get(0), name, ""));
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
        System.out.println("💾 visitLdcInsn LDC: 상수값 저장" + cst);
        valueStack.push(cst);
    }

    /**
     * 단일 int 피연산자를 갖는 명령어를 방문합니다.
     *
     * @param opcode 방문할 명령어의 opcode입니다. 이 opcode는 BIPUSH, SIPUSH 또는 NEWARRAY 중 하나입니다.
     * @param operand 방문할 명령어의 피연산자입니다.<br>
     *     opcode가 BIPUSH인 경우, operand 값은 Byte.MIN_VALUE와 Byte.MAX_VALUE 사이여야 합니다.
     *     <br>
     *     opcode가 SIPUSH인 경우, operand 값은 Short.MIN_VALUE와 Short.MAX_VALUE 사이여야 합니다.
     *     <br>
     *     opcode가 NEWARRAY인 경우, operand 값은 {@link Opcodes#T_BOOLEAN}, {@link
     *     Opcodes#T_CHAR}, {@link Opcodes#T_FLOAT}, {@link Opcodes#T_DOUBLE}, {@link Opcodes#T_BYTE},
     *     {@link Opcodes#T_SHORT}, {@link Opcodes#T_INT} 또는 {@link Opcodes#T_LONG} 중 하나여야 합니다.
     */
    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        System.out.println("visitIntInsn "+opcode + " opcode=" + OPCODES[opcode] + ", operand=" + operand);
        valueStack.push(operand);
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
                if(valueStack.peek() instanceof LabelInfo labelInfo) {
                    //labelInfo 다음 ICONST_0는 false를 뜻함
                    labelInfo.value(false);
                    valueStack.pop();
                    return;
                }
                valueStack.push(0);
            }
            case ICONST_1 -> {
                System.out.println("🧱 ICONST_1 → push 1");
                if(valueStack.peek() instanceof LabelInfo labelInfo) {
                    //true를 뜻함
                    labelInfo.value(true);

                    valueStack.pop();
                    return;
                }
                valueStack.push(1);
            }
            case ICONST_2, ICONST_3, ICONST_4, ICONST_5 -> {
                valueStack.push(opcode - (ICONST_5 - ICONST_2));
                System.out.println("🧱 "+ OPCODES[opcode] +" → push "+(opcode -3));
            }
            case LCONST_0, LCONST_1 -> {
                long l = opcode - LCONST_0;
                System.out.println("🧱 LCONST_x → push "+l);
                valueStack.push(l);
            }
            case FCONST_0, FCONST_1, FCONST_2 -> {
                float f = opcode - FCONST_0;
                System.out.println("🧱 FCONST_x → push "+f);
                valueStack.push(f);
            }
            case DCONST_0, DCONST_1 -> {
                double d = opcode - DCONST_0;
                System.out.println("🧱 DCONST_x → push "+d);
                valueStack.push(d);
            }
            case IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB,
                 IMUL, LMUL, FMUL, DMUL, IDIV, LDIV, FDIV, DDIV,
                 IREM, LREM, DREM, FREM,
                 IAND, LAND, IOR, LOR, IXOR, LXOR-> {
                Object right = valueStack.pop();
                Object left = valueStack.pop();
                BinaryOperator.fromOpcode(opcode);
                BinaryCondition condition = BinaryCondition.of(left, BinaryOperator.fromOpcode(opcode), right);
                valueStack.push(condition);
            }
//            case IAND -> {
//                pushLogicalExpr(LogicalOperator.AND, exprStack.pop(), exprStack.pop());
//            }
//            case IOR -> {
//                pushLogicalExpr(LogicalOperator.OR, exprStack.pop(), exprStack.pop());
//            }

            case I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I,
                 D2L, D2F, I2B, I2C, I2S -> {
                //형변환
            }
            case FCMPG, FCMPL  -> {
                //FCMPG or FCMPL + IFLT
                Object right = valueStack.pop();
                Object left = valueStack.pop();
                Comparison comparison = Comparison.of(left, right);
                valueStack.push(comparison);
                System.out.println("🧮 "+OPCODES[opcode]+" → push valueStack Comparison(" + left + ", " + right + ")");
            }
            case DCMPG, DCMPL -> {
                //DCMPG or DCMPL + IFLT
                Object right = valueStack.pop();
                Object left = valueStack.pop();
                Comparison comparison = Comparison.of(left, right);
                valueStack.push(comparison);
                System.out.println("🧮 "+OPCODES[opcode]+" → push valueStack Comparison(" + left + ", " + right + ")");
            }
            case LCMP -> {
                //long: LCMP + IFLT/IFGT
                //LCMP는 항상 비교 조건으로 변환되어야 하므로, 조건 분기 없이 쓰이는 LCMP는 분석 대상에서 제외
                //두 개의 long 값을 비교해서, 결과를 int로 푸시하는 비교 전용 명령어로 같으면 0, 왼쪽이 크면 1, 오른쪽이 크면 -1을 푸시합니다.
                Object right = valueStack.pop();
                Object left = valueStack.pop();
                Comparison comparison = Comparison.of(left, right);
                valueStack.push(comparison);
                //값 비교는 0,1,-1 을 반환하므로 IFXX lable 이 따라옴
                System.out.println("🧮 LCMP → push valueStack Comparison(" + left + ", " + right + ")");
            }
//            case ICONST_0, ICONST_1 -> valueStack.push(opcode == ICONST_1);
            case IRETURN,ARETURN -> {
                if(valueStack.peek() instanceof LabelInfo labelInfo) {
                    labelInfo.value(opcode);
                    valueStack.pop();
                    valueStack.push(Return.of(labelInfo));
                }

                System.out.println("🔚 IRETURN,ARETURN: return exprStack pop");
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
     *     - IFEQ: 스택의 값이 0인지 비교하여 조건 분기. IF Equal to Zero
     *     - IFNE: 스택의 값이 0이 아닌지 비교하여 조건 분기. IF Not Equal to Zero
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
        System.out.println("//visitJumpInsn:" + OPCODES[opcode]+ " labelInfo=" + label);
        switch (opcode) {
            case IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPLE, IF_ICMPGT, IF_ICMPGE -> {
                //int, boolean, byte, char, short 는 내부적으로 모두 int로 변환되기 때문에
                //별도 ICMP는 없고 바로 IF_ICMPxx 조건 분기 명령으로 처리
//                if (valueStack.size() < 2) {
//                    //비교 구문이므로 두개의 stack이 필요
//                    System.err.println("❌ Stack too small at jump opcode: " + opcode);
//                    new RuntimeException().printStackTrace();
//                    return;
//                }
                LabelInfo labelInfo = labels.computeIfAbsent(label, k -> LabelInfo.of(label, null));

                Object right = valueStack.pop();
                Object left = valueStack.pop();
                BinaryOperator operator = BinaryOperator.fromOpcode(opcode);

                ComparisonBinaryCondition condition = ComparisonBinaryCondition.of(left, operator, right, labelInfo);
                valueStack.push(condition);
                exprStack.add(condition);

                System.out.println("✅ 비교 조건 추가됨: " + left + " " + operator.symbol() + " " + right);
            }
            case IFEQ, IFNE, IFLT, IFLE, IFGT, IFGE -> {
                //IFGT > , IFGE >=, IFLT <, IFLE <=, IFNE !=, IFEQ ==
                System.out.println("🔍 "+ OPCODES[opcode] +" detected: opcode = " + opcode + ", labelInfo = "+ label + ", stack = " + valueStack);
                if(valueStack.peek() instanceof Comparison comparison) {
                    //cmp 이후 비교 구문이 나오면 long, float, double 비교인 경우이므로 if조건 판별
                    ////0, 1, -1 가 나옴
                    LabelInfo labelInfo = labels.computeIfAbsent(label, k -> LabelInfo.of(label, null));

                    BinaryOperator operator = BinaryOperator.fromOpcode(opcode);
                    Object right = comparison.right();
                    Object left = comparison.left();

                    ComparisonBinaryCondition condition = ComparisonBinaryCondition.of(left, operator, right, labelInfo);
                    valueStack.push(condition);
                    exprStack.add(condition);

                    System.out.println("✅ 비교 조건 추가됨: " + left + " " + operator.symbol() + " " + right);
                } else if(opcode == IFEQ || opcode == IFNE) {
                    LabelInfo labelInfo = labels.computeIfAbsent(label, k -> LabelInfo.of(label, null));
                    Object left = valueStack.pop();
                    if(left instanceof Boolean || (left instanceof ObjectCapturedVariable capturedVariable && capturedVariable.isBoolean())) {
                        //boolean 타입이 하나만 있는 경우
                        UnaryOperator operator = UnaryOperator.fromOpcode(opcode);
                        UnaryCondition condition = UnaryCondition.of(left, operator, labelInfo);
                        valueStack.push(condition);
                        exprStack.add(condition);
//                        BinaryOperator operator = BinaryOperator.fromOpcode(opcode);
//                        boolean right = opcode == IFEQ ? false : false;
//                        ComparisonBinaryCondition condition = ComparisonBinaryCondition.of(left, operator, right, labelInfo);
//                        valueStack.push(condition);
//                        exprStack.add(condition);
                        return;
                    } else if(left instanceof Number || (left instanceof ObjectCapturedVariable capturedVariable && capturedVariable.isInt())) {
                        //int, long, float, double 등 숫자형 비교
                        BinaryOperator operator = BinaryOperator.fromOpcode(opcode);
                        int right = opcode == IFEQ ? 1 : 0;
                        ComparisonBinaryCondition condition = ComparisonBinaryCondition.of(left, operator, right, labelInfo);
                        valueStack.push(condition);
                        exprStack.add(condition);
                        return;
                    }
                    throw new UnsupportedOperationException("Unsupported IFEQ/IFNE condition value: " + left);
                }
            }
            case IF_ACMPEQ, IF_ACMPNE -> {
                //TODO 참조형 레퍼런스 주소 비교를 id비교로 변경
                System.out.println("🔁 " + OPCODES[opcode]+ " labelInfo=" + label);
            }
            case GOTO -> {
                //TODO goto 문을 skip할지는 나중에 검토
                LabelInfo labelInfo = labels.computeIfAbsent(label, k -> LabelInfo.of(label, null));
                Goto gotoInfo = new Goto(labelInfo);
                valueStack.push(gotoInfo);
                exprStack.add(gotoInfo);
                System.out.println("🔁 GOTO encountered: jump to labelInfo " + label);
            }
            case IFNONNULL -> {
                System.out.println("🔁 IFNONNULL encountered: jump to labelInfo " + label);
            }
            default -> {
                throw new UnsupportedOperationException("Unsupported jump opcode: " + opcode);
            }
        }
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
        System.out.println("//visitLabel: " + label);
        LabelInfo info = labels.get(label);
        if (info == null) {
            info = LabelInfo.of(label, null);
        }
        exprStack.add(info);
        valueStack.push(info);
        super.visitLabel(label);
    }

    /**
     * 바이트코드 끝을 알립니다.
     * 최종적으로 조건 표현식 등을 반환하거나 정리하는 데 사용됩니다.
     * conditionExpr 마무리 작업
     */
    @Override
    public void visitEnd() {
//        if (conditionExpr == null && !exprStack.isEmpty()) {
//            conditionExpr = exprStack.pop();
//        }
        super.visitEnd();
    }

    public ConditionExpression getConditionExpr2() {
        if (exprStack.isEmpty())
            return null;
        List<ConditionExpression> results = new ArrayList<>(exprStack.size());
        Label currentLabel = null;
        for (ConditionExpression expression : exprStack) {
            if (expression instanceof ComparisonBinaryCondition comparison) {
                Object labelValue = comparison.labelInfo().value();
                Label label = comparison.labelInfo().label();
                if (labelValue == null && currentLabel == null) {
                    results.add(RoundBracketCondition.OPEN);
                    results.add(comparison);
                    currentLabel = label;
                } else if(labelValue == null && currentLabel == label) {
                    results.add(comparison);
                } else if(labelValue == null && currentLabel != label) {
                    results.add(comparison);
                    currentLabel = null;
                    results.add(RoundBracketCondition.CLOSE);
                } else if (labelValue != null && currentLabel != null) {
                    results.add(comparison);
                    results.add(RoundBracketCondition.CLOSE);
                    currentLabel = null;
                }  else if(labelValue != null) {
                    results.add(comparison);
                    currentLabel = null;
                }

            } else if (expression instanceof UnaryCondition unary) {
                Object labelValue = unary.labelInfo().value();
                Label label = unary.labelInfo().label();
                if (labelValue == null && currentLabel == null) {
                    results.add(RoundBracketCondition.OPEN);
                    results.add(unary);
                    currentLabel = label;
                } else if(labelValue == null && (currentLabel != null && currentLabel != label)) {
                    results.add(unary);
                    results.add(RoundBracketCondition.CLOSE);
                    currentLabel = label;
                } else if (labelValue != null && currentLabel != null) {
                    results.add(unary);
                    results.add(RoundBracketCondition.CLOSE);
                    currentLabel = null;
                }  else if(labelValue != null) {
                    results.add(unary);
                    currentLabel = null;
                }
            }
        }
        for (ConditionExpression expression : exprStack) {
            if (expression instanceof ComparisonBinaryCondition comparison) {
                Object value = comparison.labelInfo().value();
                if (value instanceof Boolean b && !b) {
                    //false인 조건은 operator를 반전시킴
                    comparison.reverseOperator();
                    //라벨이 false 이면 and 조건으로 다음과 결합
                    //라벨이 true이면 or 조건으로 다음과 결합
                } else if (value == null) {
                    System.out.println("   🔄 라벨이 값이 boolean 이 아닌 조건: " + comparison.labelInfo().label());
                }
            } else if (expression instanceof UnaryCondition unary) {
                Object value = unary.labelInfo().value();
                if (value instanceof Boolean b && !b) {
                    //false인 조건은 operator를 반전시킴
                    unary.reverseOperator();
                    //라벨이 false 이면 and 조건으로 다음과 결합
                    //라벨이 true이면 or 조건으로 다음과 결합
                }
            }
        }
        //TODO labelInfo 의 값이 false 이거나 null 이면 operation은 반전 false  and 조건으로 결합
        //TODO labelInfo 의 값이 true 이면 or 조건으로 결합
        //TODO labelInfo 의 값이 null 이면 ( 를 열고  라벨의 값이 true나 false가 나올때 까지 보류, 값이 나오면 해당 값의 false 이면 반대로 or 조건으로 결합됨
        //TODO 이때 첫번째 ( 를 연 라벨과 같은 라벨 아이디와 다음 라벨 아이디까지가 종료대상이 됨(? 아직 검증 안함)
        System.out.println("exprStack: " + exprStack);
        return null;
       /* List<ConditionExpression> all = new ArrayList<>(exprStack);
        exprStack.clear();

        // 분기된 OR 조건이 포함되었는지 상태로 판단할 수 있다면 여기서 구분 처리 필요
        LogicalOperator operator = all.stream().anyMatch(expr -> expr instanceof LogicalCondition lc && lc.operator == LogicalOperator.OR)
                ? LogicalOperator.OR
                : LogicalOperator.AND;

        return all.size() == 1
                ? all.get(0)
                : new LogicalCondition(operator, all);*/
//        return conditionExpr;
    }




    public ConditionGroupNode buildFlatGroups(List<ConditionExpression> exprStack) {
        List<ConditionNode> groups = new ArrayList<>();
        List<ConditionLeafNode> buffer = new ArrayList<>();

        LabelInfo returnLabel = null;
        for (Object item : exprStack) {
            if (item instanceof ComparisonBinaryCondition cmp) {
                LabelInfo labelInfo = cmp.labelInfo(); // 추출 필요
                ConditionLeafNode leaf = new ConditionLeafNode(cmp, labelInfo);
                buffer.add(leaf);
            } else if (item instanceof LabelInfo labelInfo) {
                if(buffer.isEmpty()) {
                    if(labelInfo.value().equals(IRETURN) || labelInfo.value() instanceof Return) {
                        // 라벨이 리턴인 경우
                        // 버퍼에 있는 것들을 그룹으로 만들고 리턴
                        returnLabel = labelInfo;
                    }
                } else if(buffer.size() == 1) {
                    // 버퍼에 하나만 있으면 그룹을 만들지 않고 그냥 추가
                    ConditionLeafNode leaf = buffer.getFirst();
                    groups.add(leaf);
                    buffer.clear();
                } else {
                    // 버퍼에 여러개가 있으면 그룹을 만들어서 추가
                    // 그룹을 종료할 타이밍
                    ConditionGroupNode group = new ConditionGroupNode(labelInfo);
                    for (ConditionLeafNode leaf : buffer) {
                        group.addChild(leaf);
                    }
                    groups.add(group);
                    buffer.clear();
                }
            }
        }

        ConditionGroupNode root = ConditionGroupNode.root(returnLabel);
        for (ConditionNode group : groups) {
            root.addChild(group);
        }
        return root;
    }



    public ConditionExpression getConditionExpr() {
        if (exprStack.isEmpty()) return null;
        List<Object> list = valueStack.reversed().stream().toList();
        ConditionGroupNode root = buildFlatGroups(exprStack); // 너가 이미 만든 1차 결과
        ConditionGroupNode.makeGrouping(root);

        {
            //value stack의 값을 순환하며 and, or 및 비교 구문 정리
            LabelInfo currentLabelInfo = null;
            for(Object expression : valueStack) {
                if (expression instanceof ComparisonBinaryCondition comparison) {
                    Object labelValue = comparison.labelInfo().value();
                    Label label = comparison.labelInfo().label();
                    if (labelValue != null && labelValue instanceof Boolean b) {
                        //false인 조건은 operator를 반전시킴
                        //라벨이 false 이면 and 조건으로 다음과 결합
                        //라벨이 true이면 or 조건으로 다음과 결합
                        if(!b) {
                            comparison.reverseOperator();
                        }
                        currentLabelInfo = comparison.labelInfo();
                    }
                } else if (expression instanceof LabelInfo labelInfo && labelInfo.value() == null) {
                    labelInfo.value(currentLabelInfo.value());
                }
            }
        }
        //소괄호 처리

        //1. valueStack의 역순으로 탐색
        //2. LabelInfo가 null인 경우 그 이전 라벨까지 labelinfo 에 저장
        //3.  2항에서 넣은 ComparisonBinaryCondition 객체에 라벨이 null 이 남아 있는 경우 해당 ComparisonBinaryCondition 항과 LabelInfo 까지릃 한번 더 감싸줌
        /*for (Iterator<Object> it = valueStack.iterator(); it.hasNext(); ) {
            Object expression = it.next();

            if (expression instanceof ComparisonBinaryCondition comparison) {
                Object labelValue = comparison.labelInfo().value();
                Label label = comparison.labelInfo().label();
                if (labelValue != null && labelValue instanceof Boolean b) {
                    //false인 조건은 operator를 반전시킴
                    //라벨이 false 이면 and 조건으로 다음과 결합
                    //라벨이 true이면 or 조건으로 다음과 결합
                    if(!b) {
                        comparison.reverseOperator();
                    }
                    currentLabelInfo = comparison.labelInfo();
                }
            } else if (expression instanceof LabelInfo labelInfo && labelInfo.value() == null) {
                labelInfo.value(currentLabelInfo.value());
            }
        }*/
//        for (Iterator<Object> it = valueStack.descendingIterator(); it.hasNext(); ) {
//
//        }


        return null;
    }




    // unsupport insn


    /**
     * 타입 명령어를 방문합니다. 타입 명령어는 클래스의 내부 이름(internal name)을 매개변수로 사용하는 명령어입니다
     * (참조: {@link Type#getInternalName()}).
     *
     * @param opcode 방문할 타입 명령어의 opcode입니다. 이 opcode는 NEW, ANEWARRAY, CHECKCAST 또는 INSTANCEOF 중 하나입니다.
     * @param type 방문할 명령어의 피연산자입니다. 이 피연산자는 객체나 배열 클래스의 내부 이름이어야 합니다
     *            (참조: {@link Type#getInternalName()}).
     */
    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        System.out.println("visitTypeInsn "+opcode + " opcode=" + OPCODES[opcode] + ", type=" + type);
    }

    /**
     * 로컬 변수 슬롯에 있는 int 변수에 정수 값을 더하거나 뺄 때 사용
     * i++ 또는 i += 1 같은 코드
     * @param varIndex 로컬 변수 슬롯의 인덱스 (예: i가 몇 번 슬롯에 저장됐는지)
     * @param increment 증가 또는 감소시킬 값 (음수면 감소)
     */
    public void visitIincInsn(final int varIndex, final int increment) {
        System.out.println("visitIincInsn "+varIndex +  ", increment=" + increment);
    }

    /**
     * 동적 호출(invokedynamic) 명령어를 방문합니다.
     *
     * @param name 메서드의 이름.
     * @param descriptor 메서드의 서명(descriptor) ( {@link Type} 참조).
     * @param bootstrapMethodHandle 부트스트랩 메서드 핸들.
     * @param bootstrapMethodArguments 부트스트랩 메서드의 상수 인자들. 각 인자는 {@link Integer}, {@link Float}, {@link Long}, {@link Double}, {@link String}, {@link Type}, {@link Handle}, 또는 {@link ConstantDynamic} 값이어야 합니다. 이 메서드는 배열의 내용을 수정할 수 있으므로 호출자는 이 배열이 변경될 수 있음을 예상해야 합니다.
     */
    public void visitInvokeDynamicInsn(
            final String name,
            final String descriptor,
            final Handle bootstrapMethodHandle,
            final Object... bootstrapMethodArguments) {

        System.out.println("visitInvokeDynamicInsn(name=" + name + ", descriptor=" + descriptor +
                ", bootstrapMethodHandle=" + bootstrapMethodHandle +
                ", bootstrapMethodArguments=" + Arrays.toString(bootstrapMethodArguments) + ")");
    }


    /**
     * Visits a TABLESWITCH instruction.
     * Java 바이트코드에서 tableswitch 명령어를 만났을 때 호출됩니다.
     * 이 명령어는 switch 문 중에서도 case 값들이 연속된 정수 범위일 때 사용
     * @param min the minimum key value.
     * @param max the maximum key value.
     * @param dflt beginning of the default handler block.
     * @param labels beginnings of the handler blocks. {@code labels[i]} is the beginning of the
     *     handler block for the {@code min + i} key.
     */
    public void visitTableSwitchInsn(
            final int min, final int max, final Label dflt, final Label... labels) {
        System.out.println("visitTableSwitchInsn "+min + " max=" + max + ", dflt=" + dflt);
        throw new UnsupportedOperationException("visitTableSwitchInsn");
    }

    /**
     * LOOKUPSWITCH 명령어를 방문합니다.
     *
     * @param dflt 기본 처리 블록의 시작 위치입니다.
     * @param keys 키 값들입니다.
     * @param labels 각 키에 해당하는 처리 블록들의 시작 위치입니다.
     */
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
        System.out.println("visitLookupSwitchInsn 호출됨 | dflt: " + dflt + ", keys: " + Arrays.toString(keys) + ", labels: " + Arrays.toString(labels));
    }

    /**
     * MULTIANEWARRAY 명령어를 방문합니다.
     *
     * @param descriptor 배열 타입 디스크립터입니다.
     * @param numDimensions 생성할 배열의 차원 수입니다.
     */
    public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions) {
        System.out.println("visitMultiANewArrayInsn 호출됨 | descriptor: " + descriptor + ", numDimensions: " + numDimensions);
    }

    /**
     * 명령어에 대한 애노테이션을 방문합니다. 이 메소드는 해당 명령어 직후에 호출되어야 합니다.
     * 동일 명령어에 대해 여러 번 호출될 수 있습니다.
     *
     * @param typeRef 애노테이션 대상 타입 참조입니다.
     * @param typePath 타입 내의 경로입니다.
     * @param descriptor 애노테이션 클래스의 디스크립터입니다.
     * @param visible 런타임에 애노테이션이 보이는지 여부입니다.
     */
    public AnnotationVisitor visitInsnAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
        System.out.println("visitInsnAnnotation 호출됨 | typeRef: " + typeRef + ", typePath: " + typePath + ", descriptor: " + descriptor + ", visible: " + visible);
        return null;
    }

    /**
     * try-catch 블록을 방문합니다.
     *
     * @param start 예외 핸들러 범위의 시작 (포함).
     * @param end 예외 핸들러 범위의 끝 (제외).
     * @param handler 예외 핸들러 코드의 시작.
     * @param type 처리할 예외 타입의 내부 이름, null일 경우 모든 예외를 처리합니다 (finally 블록).
     */
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        System.out.println("visitTryCatchBlock 호출됨 | start: " + start + ", end: " + end + ", handler: " + handler + ", type: " + type);
    }

    /**
     * 예외 핸들러 타입에 대한 애노테이션을 방문합니다. 반드시 visitTryCatchBlock 호출 이후에 호출되어야 합니다.
     *
     * @param typeRef 타입 참조입니다.
     * @param typePath 타입 경로입니다.
     * @param descriptor 애노테이션 클래스 디스크립터입니다.
     * @param visible 런타임에 보이는지 여부입니다.
     */
    public AnnotationVisitor visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
        System.out.println("visitTryCatchAnnotation 호출됨 | typeRef: " + typeRef + ", typePath: " + typePath + ", descriptor: " + descriptor + ", visible: " + visible);
        return null;
    }

    /**
     * 애노테이션 인터페이스 메소드의 기본 값을 방문합니다.
     *
     * @return 애노테이션 기본값을 방문할 visitor. visit 메소드는 정확히 한 번 호출되어야 하며, 이후 visitEnd가 따라야 합니다.
     */
    public AnnotationVisitor visitAnnotationDefault() {
        System.out.println("visitAnnotationDefault 호출됨");
        return null;
    }

    /**
     * 메소드에 대한 애노테이션을 방문합니다.
     *
     * @param descriptor 애노테이션 클래스의 디스크립터입니다.
     * @param visible 런타임에 보이는지 여부입니다.
     */
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
        System.out.println("visitAnnotation 호출됨 | descriptor: " + descriptor + ", visible: " + visible);
        return null;
    }

    /**
     * 메소드 시그니처의 타입에 대한 애노테이션을 방문합니다.
     *
     * @param typeRef 타입 참조입니다.
     * @param typePath 타입 경로입니다.
     * @param descriptor 애노테이션 클래스 디스크립터입니다.
     * @param visible 런타임에 보이는지 여부입니다.
     */
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
        System.out.println("visitTypeAnnotation 호출됨 | typeRef: " + typeRef + ", typePath: " + typePath + ", descriptor: " + descriptor + ", visible: " + visible);
        return null;
    }

    /**
     * 애노테이션을 가질 수 있는 메소드 파라미터 수를 방문합니다.
     *
     * @param parameterCount 애노테이션이 가능한 파라미터 수입니다.
     * @param visible 런타임에 보이는지 여부입니다.
     */
    public void visitAnnotableParameterCount(final int parameterCount, final boolean visible) {
        System.out.println("visitAnnotableParameterCount 호출됨 | parameterCount: " + parameterCount + ", visible: " + visible);
    }

    /**
     * 메소드 파라미터에 대한 애노테이션을 방문합니다.
     *
     * @param parameter 파라미터 인덱스입니다.
     * @param descriptor 애노테이션 클래스의 디스크립터입니다.
     * @param visible 런타임에 보이는지 여부입니다.
     */
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String descriptor, final boolean visible) {
        System.out.println("visitParameterAnnotation 호출됨 | parameter: " + parameter + ", descriptor: " + descriptor + ", visible: " + visible);
        return null;
    }

    /**
     * 메소드의 사용자 정의 속성을 방문합니다.
     *
     * @param attribute 속성입니다.
     */
    public void visitAttribute(final org.objectweb.asm.Attribute attribute) {
        System.out.println("visitAttribute 호출됨 | attribute: " + attribute);
    }

    /**
     * 지역 변수 타입에 대한 애노테이션을 방문합니다.
     *
     * @param typeRef 타입 참조입니다.
     * @param typePath 타입 경로입니다.
     * @param start 지역 변수 범위의 시작 지점들입니다.
     * @param end 지역 변수 범위의 끝 지점들입니다.
     * @param index 지역 변수 인덱스들입니다.
     * @param descriptor 애노테이션 클래스 디스크립터입니다.
     * @param visible 런타임에 보이는지 여부입니다.
     */
    public AnnotationVisitor visitLocalVariableAnnotation(
            final int typeRef,
            final TypePath typePath,
            final Label[] start,
            final Label[] end,
            final int[] index,
            final String descriptor,
            final boolean visible) {
        System.out.println("visitLocalVariableAnnotation 호출됨 | typeRef: " + typeRef + ", typePath: " + typePath + ", start: " + Arrays.toString(start) + ", end: " + Arrays.toString(end) + ", index: " + Arrays.toString(index) + ", descriptor: " + descriptor + ", visible: " + visible);
        return null;
    }

    /**
     * 라인 넘버 선언을 방문합니다.
     *
     * @param line 소스 파일의 라인 번호입니다.
     * @param start 이 라인에 해당하는 첫 번째 명령어입니다.
     */
    public void visitLineNumber(final int line, final Label start) {
        System.out.println("visitLineNumber 호출됨 | line: " + line + ", start: " + start);
    }

    /**
     * 메소드의 최대 스택 크기와 지역 변수 수를 방문합니다.
     *
     * @param maxStack 최대 스택 크기입니다.
     * @param maxLocals 최대 지역 변수 수입니다.
     */
    public void visitMaxs(final int maxStack, final int maxLocals) {
        System.out.println("visitMaxs 호출됨 | maxStack: " + maxStack + ", maxLocals: " + maxLocals);
    }


   /* private String resolveColumnNameRecursive(Class<?> currentClass, String fieldName, String prefix) {
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
    }*/

//    private String resolveColumnNameFromGetter(String owner, String methodName) {
////        if (!owner.replace("/", ".").equals(entityClass.getName())) {
////            return null;
////        }
//
//        String fieldName = null;
//        if (methodName.startsWith("get") && methodName.length() > 3) {
//            fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
//        } else if (methodName.startsWith("is") && methodName.length() > 2) {
//            fieldName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
//        }
////        if (fieldName != null) {
////            return resolveColumnNameRecursive(entityClass, fieldName, "");
////        }
//        return null;
//    }



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
}