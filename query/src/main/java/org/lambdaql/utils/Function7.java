package org.lambdaql.utils;

@FunctionalInterface
public interface Function7<T1, T2, T3, T4, T5, T6, T7, R> {
    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7);

    default <V> Function7<T1, T2, T3, T4, T5, T6, T7, V> andThen(java.util.function.Function<? super R, ? extends V> after) {
        return (T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7) -> after.apply(apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7));
    }
}
