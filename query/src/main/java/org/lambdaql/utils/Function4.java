package org.lambdaql.utils;

@FunctionalInterface
public interface Function4<T1, T2, T3, T4, R> {
    R apply(T1 t1, T2 t2, T3 t3, T4 t4);

    default <V> Function4<T1, T2, T3, T4, V> andThen(java.util.function.Function<? super R, ? extends V> after) {
        return (T1 t1, T2 t2, T3 t3, T4 t4) -> after.apply(apply(T1 t1, T2 t2, T3 t3, T4 t4));
    }
}
