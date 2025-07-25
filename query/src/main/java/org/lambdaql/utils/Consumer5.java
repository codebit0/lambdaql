package org.lambdaql.utils;

@FunctionalInterface
public interface Consumer5<T1, T2, T3, T4, T5> {
    void accept(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);

    default Consumer5<T1, T2, T3, T4, T5> andThen(Consumer5<T1, T2, T3, T4, T5> after) {
        return (T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) -> {
            accept(t1, t2, t3, t4, t5);
            after.accept(t1, t2, t3, t4, t5);
        };
    }
}
