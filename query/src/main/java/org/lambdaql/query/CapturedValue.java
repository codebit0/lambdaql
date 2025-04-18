package org.lambdaql.query;

import java.lang.reflect.Type;

public record CapturedValue(Type type, Object value, int capturedIndex, int opcodeIndex) {

}
