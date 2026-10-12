package com.exchange.model;

/**
 * Concrete Subclass representing a SELL Order (Ask).
 * Demonstrates inheritance from {@link Order} and polymorphic behavior
 * for Ask matching and Price-Time priority sorting (lowest price first).
 */
public class SellOrder extends Order {
    private static final long serialVersionUID = 1L;

    public SellOrder(String orderId, String userId, String symbol, OrderType type,
                     double price, int quantity, long timestampEpoch) {
        super(orderId, userId, symbol, OrderSide.SELL, type, price, quantity, timestampEpoch);
    }

    public SellOrder(String orderId, String userId, String symbol, OrderType type,
                     double price, int quantity, int remainingQuantity, OrderStatus status,
                     long timestampEpoch) {
        super(orderId, userId, symbol, OrderSide.SELL, type, price, quantity,
              remainingQuantity, status, timestampEpoch);
    }

    /**
     * Polymorphic implementation of matching criteria for Asks.
     * A Sell order matches a Buy order if:
     * 1. The counterparty is on the BUY side.
     * 2. Either order is a MARKET order, or Buy price >= Sell price.
     */
    @Override
    public boolean isMatchableWith(Order oppositeOrder) {
        if (oppositeOrder == null || oppositeOrder.getSide() != OrderSide.BUY) {
            return false;
        }
        if (this.getType() == OrderType.MARKET || oppositeOrder.getType() == OrderType.MARKET) {
            return true;
        }
        return oppositeOrder.getPrice() >= this.getPrice();
    }

    /**
     * Polymorphic Price-Time Priority for Asks:
     * 1. MARKET orders take precedence over LIMIT orders.
     * 2. Lower Limit Price has higher priority (ascending price order).
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

        // For limit orders: lower price comes first (ascending)
        int priceComparison = Double.compare(this.getPrice(), other.getPrice());
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
