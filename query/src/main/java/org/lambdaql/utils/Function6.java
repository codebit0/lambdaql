package org.lambdaql.utils;

@FunctionalInterface
public interface Function6<T1, T2, T3, T4, T5, T6, R> {
    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6);

    default <V> Function6<T1, T2, T3, T4, T5, T6, V> andThen(java.util.function.Function<? super R, ? extends V> after) {
        return (T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) -> after.apply(apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6));
    }
}
