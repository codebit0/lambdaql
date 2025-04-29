package org.lambdaql.query;

import java.io.Serializable;
import java.util.function.Supplier;

public sealed interface Direction permits Direction.ASC, Direction.DESC {

    public static void direction(){

    }

    public static <T> ASC asc(Supplier<T> method) {
        return new ASC(method);
    }

    public static <T> DESC desc(Supplier<T> method) {
        return new DESC(method);
    }

    public final class ASC implements Direction, Serializable {
        public <T> ASC(Supplier<T> method) {

        }
    }

    public final class DESC implements Direction, Serializable {
        public <T> DESC(Supplier<T> method) {

        }
    }
}
