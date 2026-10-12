package com.exchange.engine;

import com.exchange.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Core Electronic Matching Engine executing Price-Time Priority matching.
 * Implements strict concurrency safety using per-symbol reentrant locks,
 * ensuring non-blocking parallel processing across independent securities
 * while guaranteeing absolute serial order matching within any single security.
 */
public class MatchingEngine {
    private final ConcurrentHashMap<String, OrderBook> books = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> symbolLocks = new ConcurrentHashMap<>();

    public MatchingEngine() {
    }

    /**
     * Resolves the OrderBook for a security symbol, creating it if not already present.
     */
    public OrderBook getOrCreateBook(String symbol) {
        String sym = symbol.toUpperCase().trim();
        return books.computeIfAbsent(sym, OrderBook::new);
    }

    private ReentrantLock getSymbolLock(String symbol) {
        String sym = symbol.toUpperCase().trim();
        return symbolLocks.computeIfAbsent(sym, s -> new ReentrantLock(true));
    }

    /**
     * Ingests and matches an incoming order against the resting order book.
     * Enforces the Maker-Price execution rule: trades execute at the price
     * of the order already resting on the book.
     *
     * @param incomingOrder incoming BuyOrder or SellOrder.
     * @return MatchResult containing executed trades, updated orders, and book status.
     */
    public MatchResult processOrder(Order incomingOrder) {
        if (incomingOrder == null) {
            throw new IllegalArgumentException("Incoming order cannot be null");
        }

        String symbol = incomingOrder.getSymbol().toUpperCase().trim();
        ReentrantLock lock = getSymbolLock(symbol);
        lock.lock();
        try {
            OrderBook book = getOrCreateBook(symbol);
            MatchResult result = new MatchResult(incomingOrder);

            if (incomingOrder.getSide() == OrderSide.BUY) {
                matchBuyOrder((BuyOrder) incomingOrder, book, result);
            } else {
                matchSellOrder((SellOrder) incomingOrder, book, result);
            }

            return result;
        } finally {
            lock.unlock();
        }
    }

    private void matchBuyOrder(BuyOrder incomingBuy, OrderBook book, MatchResult result) {
        while (incomingBuy.getRemainingQuantity() > 0) {
            SellOrder bestAsk = book.peekBestAsk();
            if (bestAsk == null || !incomingBuy.isMatchableWith(bestAsk)) {
                break;
            }

            // Remove resting ask for matching
            bestAsk = book.pollBestAsk();
            if (bestAsk == null) break;

            int matchQuantity = Math.min(incomingBuy.getRemainingQuantity(), bestAsk.getRemainingQuantity());
            
            // Trade execution price is Maker Price (price of the resting counterparty order)
            double executionPrice = bestAsk.getPrice();
            if (executionPrice <= 0.0) {
                // If resting order was market or zero, use incoming price or last price
                executionPrice = incomingBuy.getPrice() > 0 ? incomingBuy.getPrice() : book.getLastTradedPrice();
                if (executionPrice <= 0.0) executionPrice = 100.0;
            }

            // Fill quantities
            incomingBuy.fill(matchQuantity);
            bestAsk.fill(matchQuantity);

            // Construct Trade record
            String tradeId = "trd-" + UUID.randomUUID().toString().substring(0, 8);
            Trade trade = new Trade(
                    tradeId,
                    book.getSymbol(),
                    incomingBuy.getOrderId(),
                    bestAsk.getOrderId(),
                    incomingBuy.getUserId(),
                    bestAsk.getUserId(),
                    executionPrice,
                    matchQuantity,
                    System.currentTimeMillis()
            );

            result.addTrade(trade, bestAsk);
            book.setLastTradedPrice(executionPrice);

            // If resting ask still has remaining quantity, put back in the book
            if (bestAsk.getRemainingQuantity() > 0) {
                book.addOrder(bestAsk);
            }
        }

        // Handle remaining quantity of incoming Buy order
        if (incomingBuy.getRemainingQuantity() > 0) {
            if (incomingBuy.getType() == OrderType.LIMIT) {
                // Limit order rests on the book
                book.addOrder(incomingBuy);
            } else {
                // Market order cancels if liquidity is exhausted
                incomingBuy.cancel();
            }
        }
    }

    private void matchSellOrder(SellOrder incomingSell, OrderBook book, MatchResult result) {
        while (incomingSell.getRemainingQuantity() > 0) {
            BuyOrder bestBid = book.peekBestBid();
            if (bestBid == null || !incomingSell.isMatchableWith(bestBid)) {
                break;
            }

            // Remove resting bid for matching
            bestBid = book.pollBestBid();
            if (bestBid == null) break;

            int matchQuantity = Math.min(incomingSell.getRemainingQuantity(), bestBid.getRemainingQuantity());

            // Trade execution price is Maker Price (price of the resting bid order)
            double executionPrice = bestBid.getPrice();
            if (executionPrice <= 0.0) {
                executionPrice = incomingSell.getPrice() > 0 ? incomingSell.getPrice() : book.getLastTradedPrice();
                if (executionPrice <= 0.0) executionPrice = 100.0;
            }

            // Fill quantities
            incomingSell.fill(matchQuantity);
            bestBid.fill(matchQuantity);

            // Construct Trade record
            String tradeId = "trd-" + UUID.randomUUID().toString().substring(0, 8);
            Trade trade = new Trade(
                    tradeId,
                    book.getSymbol(),
                    bestBid.getOrderId(),
                    incomingSell.getOrderId(),
                    bestBid.getUserId(),
                    incomingSell.getUserId(),
                    executionPrice,
                    matchQuantity,
                    System.currentTimeMillis()
            );

            result.addTrade(trade, bestBid);
            book.setLastTradedPrice(executionPrice);

            // If resting bid still has remaining quantity, put back in the book
            if (bestBid.getRemainingQuantity() > 0) {
                book.addOrder(bestBid);
            }
        }

        // Handle remaining quantity of incoming Sell order
        if (incomingSell.getRemainingQuantity() > 0) {
            if (incomingSell.getType() == OrderType.LIMIT) {
                // Limit order rests on the book
                book.addOrder(incomingSell);
            } else {
                // Market order cancels if liquidity is exhausted
                incomingSell.cancel();
            }
        }
    }

    /**
     * Cancels an open order from the book under lock.
     */
    public Order cancelOrder(String symbol, String orderId) {
        if (symbol == null || orderId == null) return null;
        String sym = symbol.toUpperCase().trim();
        ReentrantLock lock = getSymbolLock(sym);
        lock.lock();
        try {
            OrderBook book = books.get(sym);
            if (book != null) {
                return book.cancelOrder(orderId);
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Seeds or registers an existing active order directly into the book without triggering matching.
     */
    public void seedOrder(Order order) {
        if (order == null) return;
        String sym = order.getSymbol().toUpperCase().trim();
        ReentrantLock lock = getSymbolLock(sym);
        lock.lock();
        try {
            OrderBook book = getOrCreateBook(sym);
            book.addOrder(order);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns a snapshot of the order book for the given symbol.
     */
    public OrderBookSnapshot getSnapshot(String symbol, int depth) {
        String sym = symbol.toUpperCase().trim();
        OrderBook book = getOrCreateBook(sym);
        return book.getSnapshot(depth);
    }

    public Set<String> getActiveSymbols() {
        return Collections.unmodifiableSet(books.keySet());
    }

    public void clearAll() {
        for (Map.Entry<String, OrderBook> entry : books.entrySet()) {
            ReentrantLock lock = getSymbolLock(entry.getKey());
            lock.lock();
            try {
                entry.getValue().clear();
            } finally {
                lock.unlock();
            }
        }
    }
}
