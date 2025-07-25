package org.lambdaql.utils;

@FunctionalInterface
public interface Function2<T1, T2, R> {
    R apply(T1 t1, T2 t2);

    default <V> Function2<T1, T2, V> andThen(java.util.function.Function<? super R, ? extends V> after) {
        return (T1 t1, T2 t2) -> after.apply(apply(T1 t1, T2 t2));
    }
}
