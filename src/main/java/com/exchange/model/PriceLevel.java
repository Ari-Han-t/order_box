package com.exchange.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Aggregated price level in the Order Book, grouping multiple orders at the exact same price.
 */
public class PriceLevel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double price;
    private int quantity;
    private int orderCount;
    private int cumulativeQuantity;

    @JsonCreator
    public PriceLevel(@JsonProperty("price") double price,
                      @JsonProperty("quantity") int quantity,
                      @JsonProperty("orderCount") int orderCount) {
        this.price = price;
        this.quantity = quantity;
        this.orderCount = orderCount;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public int getCumulativeQuantity() {
        return cumulativeQuantity;
    }

    public void setCumulativeQuantity(int cumulativeQuantity) {
        this.cumulativeQuantity = cumulativeQuantity;
    }

    public void addOrder(int qty) {
        this.quantity += qty;
        this.orderCount++;
    }

    @Override
    public String toString() {
        return String.format("$%.2f: %d shares (%d orders)", price, quantity, orderCount);
    }
}
