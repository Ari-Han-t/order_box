package com.exchange.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract Base Class representing an Order in the Electronic Stock Exchange.
 * Encapsulates core trading order attributes and enforces business invariants.
 * Subclasses {@link BuyOrder} and {@link SellOrder} implement polymorphic matching
 * rules and order book priority comparisons.
 */
public abstract class Order implements Comparable<Order>, Serializable {
    private static final long serialVersionUID = 1L;

    private final String orderId;
    private final String userId;
    private final String symbol;
    private final OrderSide side;
    private final OrderType type;
    private final double price;
    private final int quantity;
    private int remainingQuantity;
    private OrderStatus status;
    private final long timestampEpoch;

    /**
     * Primary constructor enforcing domain validation rules.
     */
    protected Order(String orderId, String userId, String symbol, OrderSide side,
                    OrderType type, double price, int quantity, long timestampEpoch) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (side == null) {
            throw new IllegalArgumentException("Order side cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Order type cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Order quantity must be strictly positive: " + quantity);
        }
        if (type == OrderType.LIMIT && price <= 0.0) {
            throw new IllegalArgumentException("Limit order price must be strictly positive: " + price);
        }

        this.orderId = orderId;
        this.userId = userId;
        this.symbol = symbol.toUpperCase().trim();
        this.side = side;
        this.type = type;
        this.price = (type == OrderType.MARKET) ? 0.0 : price;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
        this.status = OrderStatus.PENDING;
        this.timestampEpoch = timestampEpoch > 0 ? timestampEpoch : System.currentTimeMillis();
    }

    /**
     * Secondary constructor for reconstructing existing orders with custom remaining quantity and status.
     */
    protected Order(String orderId, String userId, String symbol, OrderSide side,
                    OrderType type, double price, int quantity, int remainingQuantity,
                    OrderStatus status, long timestampEpoch) {
        this(orderId, userId, symbol, side, type, price, quantity, timestampEpoch);
        this.remainingQuantity = remainingQuantity;
        this.status = status;
    }

    // =========================================================================
    // Polymorphic Abstract Methods
    // =========================================================================

    /**
     * Evaluates whether this order can execute against a counterparty order.
     * Polymorphic implementation differentiates Buy vs Sell matching criteria.
     *
     * @param oppositeOrder The resting counterparty order.
     * @return true if price-crossing and compatibility conditions are satisfied.
     */
    public abstract boolean isMatchableWith(Order oppositeOrder);

    /**
     * Compares price-time priority against another order on the same side of the book.
     * Bids sort descending by price, Asks sort ascending by price; ties broken FIFO.
     *
     * @param other The order on the same side to compare against.
     * @return negative if this order has higher priority, positive if other has higher priority.
     */
    public abstract int comparePriorityTo(Order other);

    @Override
    public int compareTo(Order other) {
        if (this == other) return 0;
        if (other == null) return -1;
        return comparePriorityTo(other);
    }

    // =========================================================================
    // Domain Business Logic
    // =========================================================================

    /**
     * Deducts executed quantity from remaining quantity and updates lifecycle status.
     *
     * @param filledQty quantity to fill.
     */
    public synchronized void fill(int filledQty) {
        if (filledQty <= 0) {
            throw new IllegalArgumentException("Filled quantity must be positive: " + filledQty);
        }
        if (filledQty > this.remainingQuantity) {
            throw new IllegalArgumentException(
                    "Cannot fill " + filledQty + " shares; only " + this.remainingQuantity + " remaining");
        }

        this.remainingQuantity -= filledQty;
        if (this.remainingQuantity == 0) {
            this.status = OrderStatus.FILLED;
        } else {
            this.status = OrderStatus.PARTIALLY_FILLED;
        }
    }

    /**
     * Marks the order as CANCELLED.
     */
    public synchronized void cancel() {
        if (this.status == OrderStatus.FILLED) {
            throw new IllegalStateException("Cannot cancel a fully filled order");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public synchronized void reject() {
        this.status = OrderStatus.REJECTED;
    }

    public boolean isComplete() {
        return this.status == OrderStatus.FILLED || this.status == OrderStatus.CANCELLED || this.status == OrderStatus.REJECTED;
    }

    public boolean isOpen() {
        return this.status == OrderStatus.PENDING || this.status == OrderStatus.PARTIALLY_FILLED;
    }

    public int getFilledQuantity() {
        return this.quantity - this.remainingQuantity;
    }

    // =========================================================================
    // Encapsulated Getters & Setters
    // =========================================================================

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public synchronized int getRemainingQuantity() {
        return remainingQuantity;
    }

    public synchronized void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public synchronized OrderStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(OrderStatus status) {
        this.status = status;
    }

    public long getTimestampEpoch() {
        return timestampEpoch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return String.format("%s[%s %s %s qty=%d (rem=%d) @ $%.2f status=%s id=%s user=%s]",
                getClass().getSimpleName(), side, type, symbol, quantity, remainingQuantity,
                price, status, orderId, userId);
    }
}
