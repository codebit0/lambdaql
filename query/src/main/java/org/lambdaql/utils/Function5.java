package org.lambdaql.utils;

@FunctionalInterface
public interface Function5<T1, T2, T3, T4, T5, R> {
    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);

    default <V> Function5<T1, T2, T3, T4, T5, V> andThen(java.util.function.Function<? super R, ? extends V> after) {
        return (T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) -> after.apply(apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5));
    }
}
