package com.exchange.model;

/**
 * Lifecycle states of an order in the exchange.
 */
public enum OrderStatus {
    PENDING,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED,
    REJECTED
}
