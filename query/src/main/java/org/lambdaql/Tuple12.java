package org.lambdaql;

import org.lambdaql.utils.Consumer12;
import org.lambdaql.utils.Function12;

import java.util.List;

public record Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12) implements Tuple {

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12) {
        return new Tuple12<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12);
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
    public T10 component10() { return t10; }
    public T11 component11() { return t11; }
    public T12 component12() { return t12; }

    @Override
    public List<Object> toList() {
        return List.of(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12);
    }

    @Override
    public int arity() {
        return 12;
    }

    public <R> R map(Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> mapper) {
        return mapper.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12);
    }

    public void forEach(Consumer12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> action) {
        action.accept(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12);
    }
}
