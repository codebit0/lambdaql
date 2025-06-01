package org.lambdaql.analyzer;

public enum RoundBracket {

    OPEN("("),
    CLOSE(")");

    private final String symbol;

    RoundBracket(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }

}
