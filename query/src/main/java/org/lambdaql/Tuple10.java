package org.lambdaql;

import org.lambdaql.utils.Consumer10;
import org.lambdaql.utils.Function10;

import java.util.List;

public record Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10) implements Tuple {

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10) {
        return new Tuple10<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
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

    @Override
    public List<Object> toList() {
        return List.of(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
    }

    @Override
    public int arity() {
        return 10;
    }

    public <R> R map(Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> mapper) {
        return mapper.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
    }

    public void forEach(Consumer10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> action) {
        action.accept(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
    }
}
