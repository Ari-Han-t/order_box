package com.exchange.model;

/**
 * Concrete Subclass representing a BUY Order (Bid).
 * Demonstrates inheritance from {@link Order} and polymorphic behavior
 * for Bid matching and Price-Time priority sorting (highest price first).
 */
public class BuyOrder extends Order {
    private static final long serialVersionUID = 1L;

    public BuyOrder(String orderId, String userId, String symbol, OrderType type,
                    double price, int quantity, long timestampEpoch) {
        super(orderId, userId, symbol, OrderSide.BUY, type, price, quantity, timestampEpoch);
    }

    public BuyOrder(String orderId, String userId, String symbol, OrderType type,
                    double price, int quantity, int remainingQuantity, OrderStatus status,
                    long timestampEpoch) {
        super(orderId, userId, symbol, OrderSide.BUY, type, price, quantity,
              remainingQuantity, status, timestampEpoch);
    }

    /**
     * Polymorphic implementation of matching criteria for Bids.
     * A Buy order matches an Ask if:
     * 1. The counterparty is on the SELL side.
     * 2. Either order is a MARKET order, or the Buy price >= Sell price.
     */
    @Override
    public boolean isMatchableWith(Order oppositeOrder) {
        if (oppositeOrder == null || oppositeOrder.getSide() != OrderSide.SELL) {
            return false;
        }
        if (this.getType() == OrderType.MARKET || oppositeOrder.getType() == OrderType.MARKET) {
            return true;
        }
        return this.getPrice() >= oppositeOrder.getPrice();
    }

    /**
     * Polymorphic Price-Time Priority for Bids:
     * 1. MARKET orders take precedence over LIMIT orders.
     * 2. Higher Limit Price has higher priority (descending price order).
     * 3. Same price: Earlier timestamp has higher priority (FIFO).
     * 4. Same timestamp: Deterministic tie-breaker by orderId.
     */
    @Override
    public int comparePriorityTo(Order other) {
        if (this == other) return 0;
        if (other == null) return -1;

        // Market orders always prioritized before Limit orders
        if (this.getType() == OrderType.MARKET && other.getType() != OrderType.MARKET) {
            return -1;
        }
        if (this.getType() != OrderType.MARKET && other.getType() == OrderType.MARKET) {
            return 1;
        }

        // For limit orders: higher price comes first (descending)
        int priceComparison = Double.compare(other.getPrice(), this.getPrice());
        if (priceComparison != 0) {
            return priceComparison;
        }

        // Price is identical: earlier timestamp comes first (FIFO)
        int timeComparison = Long.compare(this.getTimestampEpoch(), other.getTimestampEpoch());
        if (timeComparison != 0) {
            return timeComparison;
        }

        // Tie breaker for consistent ordering in Set/Queue structures
        return this.getOrderId().compareTo(other.getOrderId());
    }
}
