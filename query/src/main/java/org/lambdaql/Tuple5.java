package org.lambdaql;

import org.lambdaql.utils.Consumer5;
import org.lambdaql.utils.Function5;

import java.util.List;

public record Tuple5<T1, T2, T3, T4, T5>(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) implements Tuple {

    public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {
        return new Tuple5<>(t1, t2, t3, t4, t5);
    }

        public T1 component1() { return t1; }
    public T2 component2() { return t2; }
    public T3 component3() { return t3; }
    public T4 component4() { return t4; }
    public T5 component5() { return t5; }

    @Override
    public List<Object> toList() {
        return List.of(t1, t2, t3, t4, t5);
    }

    @Override
    public int arity() {
        return 5;
    }

    public <R> R map(Function5<T1, T2, T3, T4, T5, R> mapper) {
        return mapper.apply(t1, t2, t3, t4, t5);
    }

    public void forEach(Consumer5<T1, T2, T3, T4, T5> action) {
        action.accept(t1, t2, t3, t4, t5);
    }
}
