package org.lambdaql.query;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public sealed interface OrderBy3 permits OrderBy3.ASC, OrderBy3.DESC {

    public final class ASC implements OrderBy3 {
        public static ASC of(Method method) {
            return new ASC();
        }
    }
//    public final class DESC implements OrderBy3 {
//
//        public static DESC of(Method method) {
//            return new DESC();
//        }
//    }
    @FunctionalInterface
    public non-sealed interface DESC<T> extends OrderBy3, Supplier<T>, Serializable {}
}
