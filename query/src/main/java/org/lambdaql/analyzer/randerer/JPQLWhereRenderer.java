package org.lambdaql.analyzer.randerer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.lambdaql.analyzer.*;
import org.lambdaql.analyzer.grouping.ConditionGroup;
import org.lambdaql.analyzer.grouping.ConditionLeaf;
import org.lambdaql.analyzer.grouping.ConditionNode;
import org.lambdaql.function.JpqlFunction;

import java.lang.reflect.Method;
import java.util.*;

@Accessors(fluent = true)
public class JPQLWhereRenderer {

    private static final String OPEN_BRACKET = "(";
    private static final String CLOSE_BRACKET = ")";

    @Setter
    private LambdaVariableAnalyzer lambdaVariable;

    @Getter
    private Map<String, Object> params = new HashMap<>();

    public JPQLWhereRenderer(ConditionGroup root) {
        List<Object> flattened = flattenGroups(root, 0, params);
    }

    public JPQLWhereRenderer(List<Object> flow, LambdaVariableAnalyzer lambdaVariable) {
        this.lambdaVariable = lambdaVariable;
        StringBuilder sb = new StringBuilder();
        for (Object obj : flow) {
            extracted(obj, sb);
        }
        String jpqlWhere = sb.toString();
        System.out.println("JPQL Where: " + jpqlWhere);
    }

    private void extracted(Object obj, StringBuilder sb) {
        switch (obj) {
            case RoundBracket bracket -> sb.append(bracket.symbol());
            case LogicalOperator logicalOperator -> sb.append(" ").append(logicalOperator.name()).append(" ");
            case BinaryOperator binaryOperator -> sb.append(" ").append(binaryOperator.symbol()).append(" ");
            case BinaryCondition binaryCondition -> {
                Object left = binaryCondition.left();
                extracted(left, sb);
                sb.append(" ").append(binaryCondition.operator().symbol()).append(" ");
                Object right = binaryCondition.right();
                extracted(right, sb);
            }
            case MethodStack methodStack -> sb.append(methodStackToExpr(methodStack));
            case EntityVariable entity -> sb.append(entity.alias());
            case ObjectCapturedVariable capturedVariable -> {
                String paramName = ":param" + capturedVariable.sequenceIndex();
                sb.append(paramName);
                params.put(paramName, findCaptureVarInsn(capturedVariable.sequenceIndex()));
            }
            case null, default -> sb.append(obj);
        }
    }


    private List<Object> flattenGroups(ConditionGroup group, int depth, Map<String, Object> params) {
        List<Object> results = new ArrayList<>();
        List<ConditionNode> children = group.children();
        int size = children.size();
        boolean isRoot = group.isRoot();
        if (!isRoot) {
            results.add(OPEN_BRACKET);
        }
        for (int i = 0; i < size; i++) {
            ConditionNode child = children.get(i);
            if (child instanceof ConditionGroup g) {
                List<Object> list = flattenGroups(g, depth + 1, params);
                results.addAll(list);
            } else if (child instanceof ConditionLeaf l) {
                ConditionExpression expr = l.condition();
                if (expr instanceof ComparisonBinaryCondition binary) {
                    Object left = binary.left();
                    operandParse(left);
                    Object right = binary.right();
                    if (left instanceof ObjectCapturedVariable variable) {
                        String paramName = ":param" + variable.sequenceIndex();
                        left = ":"+paramName;
                        params.put(paramName, findCaptureVarInsn(variable.sequenceIndex()));
                    }
                    results.add(left);
                    results.add(binary.operator().symbol());
                    results.add(right);
                    if(!isRoot && i == size -1) {
                        //depth 만큼 닫기 괄호 추가
                        results.add(CLOSE_BRACKET.repeat(depth));
                    }
                    if(!isRoot)
                        results.add(binary.logicalOperator().name());
                }
            }

        }

        return results;
    }

    private Object operandParse(Object expr) {
        if (expr instanceof MethodStack methodStack) {
            return methodStackToExpr(methodStack);
        } else if (expr instanceof ObjectCapturedVariable capturedVariable) {
            return capturedVariable;
        } else if(expr instanceof EntityVariable entity) {
            return entity.alias();
        } else if (expr instanceof BinaryCondition condition) {
            //엔티티 표현식은 그대로 반환
            return condition;
        }
        return expr;
    }

    private Object methodStackToExpr(MethodStack methodStack) {
        StringBuilder sb = new StringBuilder();
        if (methodStack.includeEntityVariable()) {
            //엔티티가 포함된 메서드 스택은 엔티티로 변환
            Object owner = methodStack.owner();
            if(owner instanceof EntityVariable entity) {
               sb.append(entity.alias()).append(".");
            }
            MethodSignature signature = methodStack.signature();
            Method method = signature.method();

            JpqlFunction jpqlFunction = JpqlFunction.findJpqlFunction(method);
            if (jpqlFunction != null) {
                //JPQL 함수로 변환
                for (Object arg : methodStack.args()) {
                    System.out.println("JPQL Function arg: " + arg);
                }
                String expression = jpqlFunction.getExpressionPattern();
                sb.append(expression).append(".");
//                    expression.formatted()
            } else {
                //일반 메서드 호출로 변환
                String methodName = method.getName();
                Class<?> returnType = method.getReturnType();
                methodName = methodNameToPropertyName(methodName, returnType);
                sb.append(methodName).append(".");
            }
            for (MethodStack stack : methodStack.stacks()) {
                sb.append(methodStackToExpr(stack));
            }
        } else {
            //엔티티가 포함되지 않은 메서드 스택은 일반적인 메서드 호출로 변환
            //return methodStack.toMethodCall();
        }
        String string = sb.toString().replaceAll("\\.$", "");
        return string;
    }

    private ICapturedVariable findCaptureVarInsn(int varIndex) {
        return lambdaVariable.getCapturedValue(varIndex);
    }

    private static String methodNameToPropertyName(String methodName, Class<?> returnType) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return decapitalize(methodName.substring(3));
        } else if (returnType == boolean.class || returnType == Boolean.class) {
            if (methodName.startsWith("is") && methodName.length() > 2) {
                return decapitalize(methodName.substring(2));
            } else if (methodName.startsWith("has") && methodName.length() > 3) {
                return decapitalize(methodName.substring(3));
            }
            return methodName; // void 메서드는 그대로 반환
        }
        return methodName;
    }

    private static String decapitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1))) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
