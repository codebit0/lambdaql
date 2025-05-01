package org.lambdaql.analyzer;

record Comparison(Object left, Object right) {
    public static Comparison of(Object left, Object right) {
        return new Comparison(left, right);
    }

    public Object left() {
        return left;
    }

    public Object right() {
        return right;
    }
}
