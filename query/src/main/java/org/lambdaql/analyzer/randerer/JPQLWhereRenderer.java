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
        if (methodStack.includeEntityVariable()) {
            //엔티티가 포함된 메서드 스택은 엔티티로 변환
            Object owner = methodStack.owner();
            if(owner != null && owner instanceof EntityVariable entity) {
                //엔티티 클래스가 있는 경우
                System.out.println(methodStack);
                MethodSignature signature = methodStack.signature();
                Method method = signature.method();
                JpqlFunction jpqlFunction = JpqlFunction.findJpqlFunction(method);
                if (jpqlFunction != null) {
                    //JPQL 함수로 변환
                    String expression = jpqlFunction.getExpressionPattern();
                } else {
                    //일반 메서드 호출로 변환
                    String methodName = method.getName();
                    methodName = methodNameToPropertyName(methodName);
                    String expression = entity.alias() + "." + methodName;
                    return expression;
                }
            }
        } else {
            //엔티티가 포함되지 않은 메서드 스택은 일반적인 메서드 호출로 변환
            //return methodStack.toMethodCall();
        }
        return methodStack;
    }

    private ICapturedVariable findCaptureVarInsn(int varIndex) {
        return lambdaVariable.getCapturedValueOpcodeIndex(varIndex);
    }

    private static String methodNameToPropertyName(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return decapitalize(methodName.substring(3));
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            return decapitalize(methodName.substring(2));
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
