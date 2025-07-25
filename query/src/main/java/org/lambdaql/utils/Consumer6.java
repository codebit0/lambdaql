package org.lambdaql.utils;

@FunctionalInterface
public interface Consumer6<T1, T2, T3, T4, T5, T6> {
    void accept(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6);

    default Consumer6<T1, T2, T3, T4, T5, T6> andThen(Consumer6<T1, T2, T3, T4, T5, T6> after) {
        return (T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) -> {
            accept(t1, t2, t3, t4, t5, t6);
            after.accept(t1, t2, t3, t4, t5, t6);
        };
    }
}
