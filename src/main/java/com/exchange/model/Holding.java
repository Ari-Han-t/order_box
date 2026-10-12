package com.exchange.model;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a trader's position / holding in a specific security.
 */
public class Holding implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String symbol;
    private int quantity;
    private double averageBuyPrice;

    @JsonCreator
    public Holding(@JsonProperty("symbol") String symbol,
                   @JsonProperty("quantity") int quantity,
                   @JsonProperty("averageBuyPrice") double averageBuyPrice) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        this.symbol = symbol.toUpperCase().trim();
        this.quantity = quantity;
        this.averageBuyPrice = averageBuyPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public synchronized int getQuantity() {
        return quantity;
    }

    public synchronized void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public synchronized double getAverageBuyPrice() {
        return averageBuyPrice;
    }

    public synchronized void setAverageBuyPrice(double averageBuyPrice) {
        this.averageBuyPrice = averageBuyPrice;
    }

    public synchronized double getCostBasis() {
        return quantity * averageBuyPrice;
    }

    public synchronized double getMarketValue(double currentPrice) {
        return quantity * currentPrice;
    }

    public synchronized double getUnrealizedPnL(double currentPrice) {
        return getMarketValue(currentPrice) - getCostBasis();
    }

    public synchronized double getReturnPercent(double currentPrice) {
        double cost = getCostBasis();
        if (cost <= 0.0) return 0.0;
        return (getUnrealizedPnL(currentPrice) / cost) * 100.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Holding holding)) return false;
        return Objects.equals(symbol, holding.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

    @Override
    public String toString() {
        return String.format("Holding[%s: %d shares @ avg $%.2f]", symbol, quantity, averageBuyPrice);
    }
}
