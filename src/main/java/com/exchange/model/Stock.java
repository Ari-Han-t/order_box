package com.exchange.model;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a publicly traded Security / Stock instrument on the Exchange.
 */
public class Stock implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String symbol;
    private final String companyName;
    private double currentPrice;
    private final double previousClose;
    private long totalVolume;

    @JsonCreator
    public Stock(@JsonProperty("symbol") String symbol,
                 @JsonProperty("companyName") String companyName,
                 @JsonProperty("currentPrice") double currentPrice,
                 @JsonProperty("previousClose") double previousClose,
                 @JsonProperty("totalVolume") long totalVolume) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        this.symbol = symbol.toUpperCase().trim();
        this.companyName = companyName != null ? companyName.trim() : symbol;
        this.currentPrice = currentPrice;
        this.previousClose = previousClose;
        this.totalVolume = totalVolume;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public synchronized double getCurrentPrice() {
        return currentPrice;
    }

    public synchronized void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getPreviousClose() {
        return previousClose;
    }

    public synchronized long getTotalVolume() {
        return totalVolume;
    }

    public synchronized void addVolume(long volume) {
        this.totalVolume += volume;
    }

    public synchronized double getPriceChange() {
        return currentPrice - previousClose;
    }

    public synchronized double getChangePercent() {
        if (previousClose == 0.0) return 0.0;
        return ((currentPrice - previousClose) / previousClose) * 100.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stock stock)) return false;
        return Objects.equals(symbol, stock.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

    @Override
    public String toString() {
        return String.format("%s ($%.2f, %+.2f%%)", symbol, currentPrice, getChangePercent());
    }
}
