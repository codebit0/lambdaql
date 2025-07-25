package org.lambdaql;

import org.lambdaql.utils.Consumer9;
import org.lambdaql.utils.Function9;

import java.util.List;

public record Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9) implements Tuple {

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9) {
        return new Tuple9<>(t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }

        public T1 component1() { return t1; }
    public T2 component2() { return t2; }
    public T3 component3() { return t3; }
    public T4 component4() { return t4; }
    public T5 component5() { return t5; }
    public T6 component6() { return t6; }
    public T7 component7() { return t7; }
    public T8 component8() { return t8; }
    public T9 component9() { return t9; }

    @Override
    public List<Object> toList() {
        return List.of(t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }

    @Override
    public int arity() {
        return 9;
    }

    public <R> R map(Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> mapper) {
        return mapper.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }

    public void forEach(Consumer9<T1, T2, T3, T4, T5, T6, T7, T8, T9> action) {
        action.accept(t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }
}
