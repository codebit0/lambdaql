package org.lambdaql;

import org.lambdaql.utils.Consumer4;
import org.lambdaql.utils.Function4;

import java.util.List;

public record Tuple4<T1, T2, T3, T4>(T1 t1, T2 t2, T3 t3, T4 t4) implements Tuple {

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(T1 t1, T2 t2, T3 t3, T4 t4) {
        return new Tuple4<>(t1, t2, t3, t4);
    }

        public T1 component1() { return t1; }
    public T2 component2() { return t2; }
    public T3 component3() { return t3; }
    public T4 component4() { return t4; }

    @Override
    public List<Object> toList() {
        return List.of(t1, t2, t3, t4);
    }

    @Override
    public int arity() {
        return 4;
    }

    public <R> R map(Function4<T1, T2, T3, T4, R> mapper) {
        return mapper.apply(t1, t2, t3, t4);
    }

    public void forEach(Consumer4<T1, T2, T3, T4> action) {
        action.accept(t1, t2, t3, t4);
    }
}
