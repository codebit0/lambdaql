package org.lambdaql.query;

enum BinaryOperator {
    EQ("="), NE("<>"), LT("<"), LE("<="), GT(">"), GE(">="),
    IS("IS"), IS_NOT("IS NOT"),
    IN("IN"), NOT_IN("NOT IN"),
    LIKE("LIKE"), NOT_LIKE("NOT LIKE");

    public final String symbol;

    BinaryOperator(String symbol) {
        this.symbol = symbol;
    }

    public static BinaryOperator fromSymbol(String symbol) {
        for (BinaryOperator b : BinaryOperator.values()) {
            if (b.symbol.equals(symbol)) {
                return b;
            }
        }
        throw new UnsupportedOperationException(symbol);
    }

    public BinaryOperator not() {
        switch (this) {
            case EQ: return NE;
            case NE: return EQ;
            case LT: return GE;
            case LE: return GT;
            case GT: return LE;
            case GE: return LT;
            case IS: return IS_NOT;
            case IS_NOT: return IS;
            case IN: return NOT_IN;
            case NOT_IN: return IN;
            case LIKE: return NOT_LIKE;
            case NOT_LIKE: return LIKE;
        }
        throw new UnsupportedOperationException(this.toString());
    }
}