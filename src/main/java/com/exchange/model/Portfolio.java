package com.exchange.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Encapsulates a Trader's portfolio of securities and positions.
 * Provides thread-safe methods for position tracking, share availability checks,
 * and portfolio valuation.
 */
public class Portfolio implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String userId;
    private final Map<String, Holding> holdings = new ConcurrentHashMap<>();

    public Portfolio(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public Map<String, Holding> getHoldings() {
        return Collections.unmodifiableMap(holdings);
    }

    public Collection<Holding> getHoldingsList() {
        return Collections.unmodifiableCollection(holdings.values());
    }

    public Holding getHolding(String symbol) {
        if (symbol == null) return null;
        return holdings.get(symbol.toUpperCase().trim());
    }

    public int getShares(String symbol) {
        Holding holding = getHolding(symbol);
        return holding != null ? holding.getQuantity() : 0;
    }

    public boolean hasSufficientShares(String symbol, int requestedQuantity) {
        if (requestedQuantity <= 0) return true;
        return getShares(symbol) >= requestedQuantity;
    }

    /**
     * Adds shares to the portfolio and computes new weighted average cost basis.
     */
    public synchronized void addShares(String symbol, int quantity, double pricePerShare) {
        if (quantity <= 0) return;
        String sym = symbol.toUpperCase().trim();
        Holding existing = holdings.get(sym);

        if (existing == null) {
            holdings.put(sym, new Holding(sym, quantity, pricePerShare));
        } else {
            int oldQty = existing.getQuantity();
            double oldAvg = existing.getAverageBuyPrice();
            int newQty = oldQty + quantity;
            double newAvg = ((oldQty * oldAvg) + (quantity * pricePerShare)) / newQty;
            existing.setQuantity(newQty);
            existing.setAverageBuyPrice(newAvg);
        }
    }

    /**
     * Deducts shares from the portfolio upon an executed sell trade.
     */
    public synchronized void removeShares(String symbol, int quantity) {
        if (quantity <= 0) return;
        String sym = symbol.toUpperCase().trim();
        Holding existing = holdings.get(sym);

        if (existing == null || existing.getQuantity() < quantity) {
            int available = existing != null ? existing.getQuantity() : 0;
            throw new IllegalStateException(String.format(
                    "Cannot remove %d shares of %s; only %d available", quantity, sym, available));
        }

        int remaining = existing.getQuantity() - quantity;
        if (remaining == 0) {
            holdings.remove(sym);
        } else {
            existing.setQuantity(remaining);
        }
    }

    /**
     * Replaces or registers a holding loaded from the database.
     */
    public void setHolding(Holding holding) {
        if (holding != null) {
            if (holding.getQuantity() > 0) {
                holdings.put(holding.getSymbol(), holding);
            } else {
                holdings.remove(holding.getSymbol());
            }
        }
    }

    public double getTotalMarketValue(Map<String, Double> currentPrices) {
        double total = 0.0;
        for (Holding holding : holdings.values()) {
            double price = currentPrices.getOrDefault(holding.getSymbol(), holding.getAverageBuyPrice());
            total += holding.getMarketValue(price);
        }
        return total;
    }

    public double getTotalNetWorth(double cashBalance, Map<String, Double> currentPrices) {
        return cashBalance + getTotalMarketValue(currentPrices);
    }
}
