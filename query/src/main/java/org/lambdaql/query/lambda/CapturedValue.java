package org.lambdaql.query.lambda;

import java.lang.reflect.Type;

public record CapturedValue(Type type, Object value, int sequenceIndex, int opcodeIndex) implements ICapturedValue {

}
