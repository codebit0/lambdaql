package org.lambdaql;

import org.lambdaql.utils.Consumer3;
import org.lambdaql.utils.Function3;

import java.util.List;

public record Triple<T1, T2, T3>(T1 t1, T2 t2, T3 t3) implements Tuple {

    public static <T1, T2, T3> Triple<T1, T2, T3> of(T1 t1, T2 t2, T3 t3) {
        return new Triple<>(t1, t2, t3);
    }

        public T1 component1() { return t1; }
    public T2 component2() { return t2; }
    public T3 component3() { return t3; }

    @Override
    public List<Object> toList() {
        return List.of(t1, t2, t3);
    }

    @Override
    public int arity() {
        return 3;
    }

    public <R> R map(Function3<T1, T2, T3, R> mapper) {
        return mapper.apply(t1, t2, t3);
    }

    public void forEach(Consumer3<T1, T2, T3> action) {
        action.accept(t1, t2, t3);
    }
}
