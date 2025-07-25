package org.lambdaql;

import org.lambdaql.utils.Consumer6;
import org.lambdaql.utils.Function6;

import java.util.List;

public record Tuple6<T1, T2, T3, T4, T5, T6>(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) implements Tuple {

    public static <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
        return new Tuple6<>(t1, t2, t3, t4, t5, t6);
    }

        public T1 component1() { return t1; }
    public T2 component2() { return t2; }
    public T3 component3() { return t3; }
    public T4 component4() { return t4; }
    public T5 component5() { return t5; }
    public T6 component6() { return t6; }

    @Override
    public List<Object> toList() {
        return List.of(t1, t2, t3, t4, t5, t6);
    }

    @Override
    public int arity() {
        return 6;
    }

    public <R> R map(Function6<T1, T2, T3, T4, T5, T6, R> mapper) {
        return mapper.apply(t1, t2, t3, t4, t5, t6);
    }

    public void forEach(Consumer6<T1, T2, T3, T4, T5, T6> action) {
        action.accept(t1, t2, t3, t4, t5, t6);
    }
}
