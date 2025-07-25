package org.lambdaql.utils;

@FunctionalInterface
public interface Consumer3<T1, T2, T3> {
    void accept(T1 t1, T2 t2, T3 t3);

    default Consumer3<T1, T2, T3> andThen(Consumer3<T1, T2, T3> after) {
        return (T1 t1, T2 t2, T3 t3) -> {
            accept(t1, t2, t3);
            after.accept(t1, t2, t3);
        };
    }
}
