package org.lambdaql.utils;

@FunctionalInterface
public interface Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> {
    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12);

    default <V> Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, V> andThen(java.util.function.Function<? super R, ? extends V> after) {
        return (T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12) -> after.apply(apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12));
    }
}
