package org.lambdaql.utils;

@FunctionalInterface
public interface Consumer12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> {
    void accept(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12);

    default Consumer12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> andThen(Consumer12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> after) {
        return (T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12) -> {
            accept(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12);
            after.accept(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12);
        };
    }
}
