package com.exchange.service;

import com.exchange.dao.*;
import com.exchange.db.DatabaseManager;
import com.exchange.engine.MatchingEngine;
import com.exchange.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * High-performance Trading Service orchestrating order submission, risk checks,
 * matching engine dispatch, and atomic JDBC database transactions.
 * Prevents overselling, negative balances, and race conditions.
 */
public class TradingService {
    private static final Logger logger = LoggerFactory.getLogger(TradingService.class);

    private final DatabaseManager dbManager;
    private final MatchingEngine matchingEngine;
    private final OrderDAO orderDAO;
    private final TradeDAO tradeDAO;
    private final UserDAO userDAO;
    private final StockDAO stockDAO;
    private final PortfolioDAO portfolioDAO;

    public TradingService(DatabaseManager dbManager, MatchingEngine matchingEngine,
                          OrderDAO orderDAO, TradeDAO tradeDAO, UserDAO userDAO,
                          StockDAO stockDAO, PortfolioDAO portfolioDAO) {
        this.dbManager = dbManager;
        this.matchingEngine = matchingEngine;
        this.orderDAO = orderDAO;
        this.tradeDAO = tradeDAO;
        this.userDAO = userDAO;
        this.stockDAO = stockDAO;
        this.portfolioDAO = portfolioDAO;
    }

    /**
     * Initializes the in-memory Order Books with active resting orders from the database.
     */
    public void rehydrateOrderBooksFromDB() {
        try {
            logger.info("Rehydrating Order Books with active orders from database...");
            List<Order> pending = orderDAO.findAllPendingOrders();
            for (Order order : pending) {
                matchingEngine.seedOrder(order);
            }
            logger.info("Loaded {} active resting orders into Matching Engine.", pending.size());
        } catch (SQLException e) {
            logger.error("Failed to rehydrate orders from DB: {}", e.getMessage(), e);
        }
    }

    /**
     * Submits and matches an order. Performs financial risk validation,
     * dispatches through the matching engine, and executes database state changes
     * within a single ACID transaction.
     */
    public synchronized MatchResult submitOrder(String userId, String symbol, OrderSide side,
                                                OrderType type, double price, int quantity) throws Exception {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Stock symbol is required");
        }
        String sym = symbol.toUpperCase().trim();
        Stock stock = stockDAO.findBySymbol(sym);
        if (stock == null) {
            throw new IllegalArgumentException("Unknown stock symbol: " + sym);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be strictly positive: " + quantity);
        }
        if (type == OrderType.LIMIT && price <= 0.0) {
            throw new IllegalArgumentException("Limit order price must be positive: " + price);
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        // =====================================================================
        // Financial Invariant & Risk Checks
        // =====================================================================
        if (side == OrderSide.BUY) {
            double estimatedUnitCost = (type == OrderType.LIMIT) ? price : stock.getCurrentPrice();
            double totalEstimatedCost = estimatedUnitCost * quantity;
            if (user.getBalance() < totalEstimatedCost) {
                throw new IllegalStateException(String.format(
                        "Insufficient funds: available $%.2f, required $%.2f for %d shares of %s",
                        user.getBalance(), totalEstimatedCost, quantity, sym));
            }
        } else {
            // Sell order: verify user possesses enough shares to prevent overselling
            Holding holding = portfolioDAO.getHolding(userId, sym);
            int availableShares = holding != null ? holding.getQuantity() : 0;
            if (availableShares < quantity) {
                throw new IllegalStateException(String.format(
                        "Overselling rejected: attempting to sell %d shares of %s, but only %d held",
                        quantity, sym, availableShares));
            }
        }

        // =====================================================================
        // Create Order Entity
        // =====================================================================
        String orderId = "ord-" + UUID.randomUUID().toString().substring(0, 8);
        long epochNow = System.currentTimeMillis();
        Order order;
        if (side == OrderSide.BUY) {
            order = new BuyOrder(orderId, userId, sym, type, price, quantity, epochNow);
        } else {
            order = new SellOrder(orderId, userId, sym, type, price, quantity, epochNow);
        }

        // Persist initial order
        orderDAO.saveOrder(order);

        // =====================================================================
        // Process in Matching Engine
        // =====================================================================
        MatchResult matchResult = matchingEngine.processOrder(order);

        // =====================================================================
        // Atomic Database Transaction for Trades & Balances
        // =====================================================================
        executeTradePersistence(matchResult, sym);

        return matchResult;
    }

    private void executeTradePersistence(MatchResult matchResult, String symbol) throws SQLException {
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Persist each trade and transfer funds/shares
                for (Trade trade : matchResult.getTrades()) {
                    tradeDAO.saveTrade(conn, trade);

                    double tradeCost = trade.getTotalAmount();
                    // Deduct cash from Buyer
                    userDAO.adjustBalance(conn, trade.getBuyerId(), -tradeCost);
                    // Credit cash to Seller
                    userDAO.adjustBalance(conn, trade.getSellerId(), tradeCost);

                    // Transfer shares
                    portfolioDAO.adjustHolding(conn, trade.getBuyerId(), symbol, trade.getQuantity(), trade.getPrice());
                    portfolioDAO.adjustHolding(conn, trade.getSellerId(), symbol, -trade.getQuantity(), trade.getPrice());

                    // Update stock current price & volume
                    stockDAO.updatePriceAndVolume(conn, symbol, trade.getPrice(), trade.getQuantity());
                }

                // 2. Update status of resting counterparty orders
                for (Order resting : matchResult.getCounterpartyOrdersFilled()) {
                    orderDAO.updateOrder(conn, resting);
                }
                for (Order resting : matchResult.getCounterpartyOrdersPartiallyFilled()) {
                    orderDAO.updateOrder(conn, resting);
                }

                // 3. Update status of incoming order
                orderDAO.updateOrder(conn, matchResult.getIncomingOrder());

                conn.commit();
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                logger.error("Failed executing trade persistence transaction, rolled back: {}", ex.getMessage(), ex);
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Cancels an open or partially filled order.
     */
    public synchronized Order cancelOrder(String userId, String orderId) throws Exception {
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to cancel order: " + orderId);
        }
        if (order.isComplete()) {
            throw new IllegalStateException("Order is already filled or cancelled: " + order.getStatus());
        }

        // Cancel in memory engine
        Order cancelled = matchingEngine.cancelOrder(order.getSymbol(), orderId);
        if (cancelled == null) {
            cancelled = order;
            cancelled.cancel();
        }

        // Update database
        orderDAO.updateOrder(cancelled);
        return cancelled;
    }

    public OrderBookSnapshot getOrderBookSnapshot(String symbol, int depth) {
        return matchingEngine.getSnapshot(symbol, depth > 0 ? depth : 15);
    }

    public List<Trade> getRecentTrades(String symbol, int limit) throws SQLException {
        return tradeDAO.findRecentTradesBySymbol(symbol, limit > 0 ? limit : 25);
    }

    public List<Order> getActiveOrdersForUser(String userId) throws SQLException {
        return orderDAO.findActiveOrdersByUserId(userId);
    }

    public List<Order> getOrderHistoryForUser(String userId, int limit) throws SQLException {
        return orderDAO.findOrdersByUserId(userId, limit);
    }

    public MatchingEngine getMatchingEngine() {
        return matchingEngine;
    }
}
