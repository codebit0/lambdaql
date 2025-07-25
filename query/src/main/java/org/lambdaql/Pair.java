package org.lambdaql;

import org.lambdaql.utils.Consumer2;
import org.lambdaql.utils.Function2;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public record Pair<T1, T2>(T1 t1, T2 t2) implements Tuple {

    public static <T1, T2> Pair<T1, T2> of(T1 t1, T2 t2) {
        return new Pair<>(t1, t2);
    }

        public T1 component1() { return t1; }
    public T2 component2() { return t2; }

    @Override
    public List<Object> toList() {
        return List.of(t1, t2);
    }

    @Override
    public int arity() {
        return 2;
    }

    public <R> R map(BiFunction<T1, T2, R> mapper) {
        return mapper.apply(t1, t2);
    }

    public void forEach(BiConsumer<T1, T2> action) {
        action.accept(t1, t2);
    }
}
