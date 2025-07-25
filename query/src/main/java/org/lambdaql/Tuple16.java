package org.lambdaql;

import org.lambdaql.utils.Consumer16;
import org.lambdaql.utils.Function16;

import java.util.List;

public record Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>(T1 t1, T2 t2, T3 t3, T4 t4,
                                                                                             T5 t5, T6 t6, T7 t7, T8 t8,
                                                                                             T9 t9, T10 t10, T11 t11,
                                                                                             T12 t12, T13 t13, T14 t14,
                                                                                             T15 t15,
                                                                                             T16 t16) implements Tuple {

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12, T13 t13, T14 t14, T15 t15, T16 t16) {
        return new Tuple16<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16);
    }

    public T1 component1() {
        return t1;
    }

    public T2 component2() {
        return t2;
    }

    public T3 component3() {
        return t3;
    }

    public T4 component4() {
        return t4;
    }

    public T5 component5() {
        return t5;
    }

    public T6 component6() {
        return t6;
    }

    public T7 component7() {
        return t7;
    }

    public T8 component8() {
        return t8;
    }

    public T9 component9() {
        return t9;
    }

    public T10 component10() {
        return t10;
    }

    public T11 component11() {
        return t11;
    }

    public T12 component12() {
        return t12;
    }

    public T13 component13() {
        return t13;
    }

    public T14 component14() {
        return t14;
    }

    public T15 component15() {
        return t15;
    }

    public T16 component16() {
        return t16;
    }

    @Override
    public List<Object> toList() {
        return List.of(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16);
    }

    @Override
    public int arity() {
        return 16;
    }

    public <R> R map(Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R> mapper) {
        return mapper.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16);
    }

    public void forEach(Consumer16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> action) {
        action.accept(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16);
    }
}
