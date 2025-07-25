package org.lambdaql.utils;

@FunctionalInterface
public interface Function3<T1, T2, T3, R> {
    R apply(T1 t1, T2 t2, T3 t3);

    default <V> Function3<T1, T2, T3, V> andThen(java.util.function.Function<? super R, ? extends V> after) {
        return (T1 t1, T2 t2, T3 t3) -> after.apply(apply(T1 t1, T2 t2, T3 t3));
    }
}
