package com.exchange.engine;

import com.exchange.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-Safe Electronic Order Book for a single Security Symbol.
 * Implements strict Price-Time priority queues for Buy and Sell orders.
 * Employs fair ReentrantReadWriteLock to permit concurrent snapshot reads
 * while serializing state modifications (matching, addition, cancellation).
 */
public class OrderBook {
    private final String symbol;
    private final PriorityQueue<BuyOrder> bids;
    private final PriorityQueue<SellOrder> asks;
    private final Map<String, Order> orderIndex = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    private double lastTradedPrice = 0.0;

    public OrderBook(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        this.symbol = symbol.toUpperCase().trim();
        // Bids queue ordered by BuyOrder.compareTo (highest price first, FIFO)
        this.bids = new PriorityQueue<>(Order::compareTo);
        // Asks queue ordered by SellOrder.compareTo (lowest price first, FIFO)
        this.asks = new PriorityQueue<>(Order::compareTo);
    }

    public String getSymbol() {
        return symbol;
    }

    public double getLastTradedPrice() {
        rwLock.readLock().lock();
        try {
            return lastTradedPrice;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void setLastTradedPrice(double lastTradedPrice) {
        rwLock.writeLock().lock();
        try {
            this.lastTradedPrice = lastTradedPrice;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Adds an order directly into the resting order book without matching.
     * Used for seeding, reloading state, or inserting resting limit orders.
     */
    public void addOrder(Order order) {
        if (order == null || !order.getSymbol().equalsIgnoreCase(symbol)) {
            return;
        }
        rwLock.writeLock().lock();
        try {
            if (order.isComplete()) {
                orderIndex.remove(order.getOrderId());
                return;
            }

            orderIndex.put(order.getOrderId(), order);
            if (order instanceof BuyOrder buyOrder) {
                bids.remove(buyOrder);
                bids.offer(buyOrder);
            } else if (order instanceof SellOrder sellOrder) {
                asks.remove(sellOrder);
                asks.offer(sellOrder);
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Peeks at the top Buy Order (highest bid) without removing it.
     */
    public BuyOrder peekBestBid() {
        rwLock.readLock().lock();
        try {
            return bids.peek();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Peeks at the top Sell Order (lowest ask) without removing it.
     */
    public SellOrder peekBestAsk() {
        rwLock.readLock().lock();
        try {
            return asks.peek();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Polls the top Buy Order (highest bid). Must be called under write lock or internal engine lock.
     */
    public BuyOrder pollBestBid() {
        rwLock.writeLock().lock();
        try {
            BuyOrder top = bids.poll();
            if (top != null && top.isComplete()) {
                orderIndex.remove(top.getOrderId());
            }
            return top;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Polls the top Sell Order (lowest ask). Must be called under write lock or internal engine lock.
     */
    public SellOrder pollBestAsk() {
        rwLock.writeLock().lock();
        try {
            SellOrder top = asks.poll();
            if (top != null && top.isComplete()) {
                orderIndex.remove(top.getOrderId());
            }
            return top;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Cancels an open order by ID.
     * Returns the cancelled Order, or null if not found or already filled.
     */
    public Order cancelOrder(String orderId) {
        if (orderId == null) return null;
        rwLock.writeLock().lock();
        try {
            Order order = orderIndex.get(orderId);
            if (order == null || order.isComplete()) {
                return null;
            }

            if (order instanceof BuyOrder buyOrder) {
                bids.remove(buyOrder);
            } else if (order instanceof SellOrder sellOrder) {
                asks.remove(sellOrder);
            }

            order.cancel();
            orderIndex.remove(orderId);
            return order;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public Order getOrder(String orderId) {
        return orderIndex.get(orderId);
    }

    public int getBidCount() {
        rwLock.readLock().lock();
        try {
            return bids.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public int getAskCount() {
        rwLock.readLock().lock();
        try {
            return asks.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Returns an aggregated snapshot of the book depth for presentation in the UI / API.
     * Groups orders at the same price into PriceLevel buckets.
     *
     * @param maxDepth maximum number of price levels to return per side.
     */
    public OrderBookSnapshot getSnapshot(int maxDepth) {
        rwLock.readLock().lock();
        try {
            List<PriceLevel> aggregatedBids = aggregatePriceLevels(bids, true, maxDepth);
            List<PriceLevel> aggregatedAsks = aggregatePriceLevels(asks, false, maxDepth);
            return new OrderBookSnapshot(symbol, System.currentTimeMillis(), aggregatedBids, aggregatedAsks, lastTradedPrice);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private <T extends Order> List<PriceLevel> aggregatePriceLevels(PriorityQueue<T> queue, boolean isBid, int maxDepth) {
        if (queue.isEmpty()) return Collections.emptyList();

        // Copy elements safely
        List<T> copy = new ArrayList<>(queue);
        copy.sort(Order::compareTo);

        Map<Double, PriceLevel> levelMap = new LinkedHashMap<>();
        for (T order : copy) {
            if (!order.isOpen()) continue;
            double price = order.getPrice();
            PriceLevel level = levelMap.computeIfAbsent(price, p -> new PriceLevel(p, 0, 0));
            level.addOrder(order.getRemainingQuantity());
        }

        List<PriceLevel> result = new ArrayList<>(levelMap.values());
        if (maxDepth > 0 && result.size() > maxDepth) {
            result = result.subList(0, maxDepth);
        }

        // Calculate cumulative quantities for depth visualization
        int cumulative = 0;
        for (PriceLevel pl : result) {
            cumulative += pl.getQuantity();
            pl.setCumulativeQuantity(cumulative);
        }

        return result;
    }

    /**
     * Clears all orders from the book.
     */
    public void clear() {
        rwLock.writeLock().lock();
        try {
            bids.clear();
            asks.clear();
            orderIndex.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
