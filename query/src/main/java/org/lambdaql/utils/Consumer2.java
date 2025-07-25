package org.lambdaql.utils;

@FunctionalInterface
public interface Consumer2<T1, T2> {
    void accept(T1 t1, T2 t2);

    default Consumer2<T1, T2> andThen(Consumer2<T1, T2> after) {
        return (T1 t1, T2 t2) -> {
            accept(t1, t2);
            after.accept(t1, t2);
        };
    }
}
