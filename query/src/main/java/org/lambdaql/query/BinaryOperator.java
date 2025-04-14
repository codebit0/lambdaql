package org.lambdaql.query;

enum BinaryOperator {
    EQ("="), NE("<>"), LT("<"), LE("<="), GT(">"), GE(">="),
    IS("IS"), IS_NOT("IS NOT"),
    IN("IN"), LIKE("LIKE");

    public final String symbol;

    BinaryOperator(String symbol) {
        this.symbol = symbol;
    }
}