package com.exchange.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an Executed Trade resulting from a successful match between a Buy and Sell order.
 */
public class Trade implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String tradeId;
    private final String symbol;
    private final String buyOrderId;
    private final String sellOrderId;
    private final String buyerId;
    private final String sellerId;
    private final double price;
    private final int quantity;
    private final double totalAmount;
    private final long executedAtEpoch;

    @JsonCreator
    public Trade(@JsonProperty("tradeId") String tradeId,
                 @JsonProperty("symbol") String symbol,
                 @JsonProperty("buyOrderId") String buyOrderId,
                 @JsonProperty("sellOrderId") String sellOrderId,
                 @JsonProperty("buyerId") String buyerId,
                 @JsonProperty("sellerId") String sellerId,
                 @JsonProperty("price") double price,
                 @JsonProperty("quantity") int quantity,
                 @JsonProperty("executedAtEpoch") long executedAtEpoch) {
        if (tradeId == null || tradeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Trade ID cannot be null or empty");
        }
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (price <= 0.0) {
            throw new IllegalArgumentException("Trade execution price must be positive: " + price);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Trade execution quantity must be positive: " + quantity);
        }

        this.tradeId = tradeId;
        this.symbol = symbol.toUpperCase().trim();
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.price = price;
        this.quantity = quantity;
        this.totalAmount = price * quantity;
        this.executedAtEpoch = executedAtEpoch > 0 ? executedAtEpoch : System.currentTimeMillis();
    }

    public String getTradeId() {
        return tradeId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getBuyOrderId() {
        return buyOrderId;
    }

    public String getSellOrderId() {
        return sellOrderId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public long getExecutedAtEpoch() {
        return executedAtEpoch;
    }

    public Instant getExecutedAtInstant() {
        return Instant.ofEpochMilli(executedAtEpoch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trade trade)) return false;
        return Objects.equals(tradeId, trade.tradeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeId);
    }

    @Override
    public String toString() {
        return String.format("Trade[id=%s %s %d @ $%.2f ($%.2f) Buyer=%s Seller=%s]",
                tradeId, symbol, quantity, price, totalAmount, buyerId, sellerId);
    }
}
