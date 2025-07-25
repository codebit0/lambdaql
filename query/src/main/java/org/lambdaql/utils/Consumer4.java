package org.lambdaql.utils;

@FunctionalInterface
public interface Consumer4<T1, T2, T3, T4> {
    void accept(T1 t1, T2 t2, T3 t3, T4 t4);

    default Consumer4<T1, T2, T3, T4> andThen(Consumer4<T1, T2, T3, T4> after) {
        return (T1 t1, T2 t2, T3 t3, T4 t4) -> {
            accept(t1, t2, t3, t4);
            after.accept(t1, t2, t3, t4);
        };
    }
}
