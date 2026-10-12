package com.exchange.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result returned by the Matching Engine upon processing an incoming order.
 * Contains all generated trades and order modifications for downstream persistence.
 */
public class MatchResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Order incomingOrder;
    private final List<Trade> trades = new ArrayList<>();
    private final List<Order> counterpartyOrdersFilled = new ArrayList<>();
    private final List<Order> counterpartyOrdersPartiallyFilled = new ArrayList<>();
    private int totalSharesExecuted = 0;
    private double totalExecutedAmount = 0.0;

    public MatchResult(Order incomingOrder) {
        this.incomingOrder = incomingOrder;
    }

    public void addTrade(Trade trade, Order counterpartyOrder) {
        if (trade == null) return;
        this.trades.add(trade);
        this.totalSharesExecuted += trade.getQuantity();
        this.totalExecutedAmount += trade.getTotalAmount();

        if (counterpartyOrder != null) {
            if (counterpartyOrder.isComplete()) {
                if (!counterpartyOrdersFilled.contains(counterpartyOrder)) {
                    counterpartyOrdersFilled.add(counterpartyOrder);
                }
            } else {
                if (!counterpartyOrdersPartiallyFilled.contains(counterpartyOrder)) {
                    counterpartyOrdersPartiallyFilled.add(counterpartyOrder);
                }
            }
        }
    }

    public Order getIncomingOrder() {
        return incomingOrder;
    }

    public List<Trade> getTrades() {
        return Collections.unmodifiableList(trades);
    }

    public List<Order> getCounterpartyOrdersFilled() {
        return Collections.unmodifiableList(counterpartyOrdersFilled);
    }

    public List<Order> getCounterpartyOrdersPartiallyFilled() {
        return Collections.unmodifiableList(counterpartyOrdersPartiallyFilled);
    }

    public int getTotalSharesExecuted() {
        return totalSharesExecuted;
    }

    public double getTotalExecutedAmount() {
        return totalExecutedAmount;
    }

    public boolean hasTrades() {
        return !trades.isEmpty();
    }

    public boolean isFullyFilled() {
        return incomingOrder != null && incomingOrder.getStatus() == OrderStatus.FILLED;
    }

    public double getAverageExecutionPrice() {
        if (totalSharesExecuted == 0) return 0.0;
        return totalExecutedAmount / totalSharesExecuted;
    }
}
