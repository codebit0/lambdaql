package org.lambdaql;

import org.lambdaql.utils.Consumer8;
import org.lambdaql.utils.Function8;

import java.util.List;

public record Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8) implements Tuple {

    public static <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8) {
        return new Tuple8<>(t1, t2, t3, t4, t5, t6, t7, t8);
    }

        public T1 component1() { return t1; }
    public T2 component2() { return t2; }
    public T3 component3() { return t3; }
    public T4 component4() { return t4; }
    public T5 component5() { return t5; }
    public T6 component6() { return t6; }
    public T7 component7() { return t7; }
    public T8 component8() { return t8; }

    @Override
    public List<Object> toList() {
        return List.of(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    @Override
    public int arity() {
        return 8;
    }

    public <R> R map(Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> mapper) {
        return mapper.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    public void forEach(Consumer8<T1, T2, T3, T4, T5, T6, T7, T8> action) {
        action.accept(t1, t2, t3, t4, t5, t6, t7, t8);
    }
}
