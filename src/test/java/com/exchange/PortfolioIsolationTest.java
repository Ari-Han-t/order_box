package com.exchange;

import com.exchange.dao.*;
import com.exchange.db.DatabaseManager;
import com.exchange.engine.MatchingEngine;
import com.exchange.model.*;
import com.exchange.service.PortfolioService;
import com.exchange.service.TradingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioIsolationTest {
    private DatabaseManager dbManager;
    private TradingService tradingService;
    private PortfolioService portfolioService;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        dbManager = DatabaseManager.getInstance();
        MatchingEngine engine = new MatchingEngine();
        UserDAO uDao = new UserDAO(dbManager);
        StockDAO sDao = new StockDAO(dbManager);
        OrderDAO oDao = new OrderDAO(dbManager);
        TradeDAO tDao = new TradeDAO(dbManager);
        PortfolioDAO pDao = new PortfolioDAO(dbManager);

        this.userDAO = uDao;
        this.portfolioService = new PortfolioService(pDao, uDao, sDao);
        this.tradingService = new TradingService(dbManager, engine, oDao, tDao, uDao, sDao, pDao);
        this.tradingService.rehydrateOrderBooksFromDB();
    }

    @Test
    @DisplayName("Financial Invariant: Overselling is rejected when trader has insufficient shares")
    void testOversellPrevention() {
        // User 'alice' has 150 AAPL shares seeded. Attempting to sell 5,000 shares must be rejected!
        User alice = assertDoesNotThrow(() -> userDAO.findByUsername("alice"));
        assertNotNull(alice);

        Exception ex = assertThrows(IllegalStateException.class, () -> {
            tradingService.submitOrder(alice.getUserId(), "AAPL", OrderSide.SELL, OrderType.LIMIT, 200.0, 5000);
        });

        assertTrue(ex.getMessage().contains("Overselling rejected"), "Must indicate overselling error: " + ex.getMessage());
    }

    @Test
    @DisplayName("Financial Invariant: Buy orders exceeding available cash balance are rejected")
    void testInsufficientCashPrevention() {
        // User 'bob' has $50,000 seeded. Attempting to buy $10,000,000 worth of AAPL must be rejected!
        User bob = assertDoesNotThrow(() -> userDAO.findByUsername("bob"));
        assertNotNull(bob);

        Exception ex = assertThrows(IllegalStateException.class, () -> {
            tradingService.submitOrder(bob.getUserId(), "AAPL", OrderSide.BUY, OrderType.LIMIT, 200.0, 50000);
        });

        assertTrue(ex.getMessage().contains("Insufficient funds"), "Must indicate insufficient funds error: " + ex.getMessage());
    }

    @Test
    @DisplayName("Account Isolation: User cannot cancel an order belonging to another trader")
    void testOrderCancellationIsolation() throws Exception {
        User alice = userDAO.findByUsername("alice");
        User bob = userDAO.findByUsername("bob");

        // Alice places an order
        MatchResult res = tradingService.submitOrder(alice.getUserId(), "MSFT", OrderSide.BUY, OrderType.LIMIT, 100.0, 10);
        String orderId = res.getIncomingOrder().getOrderId();

        // Bob attempts to cancel Alice's order -> MUST throw Unauthorized!
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            tradingService.cancelOrder(bob.getUserId(), orderId);
        });

        assertTrue(ex.getMessage().contains("Unauthorized"), "Must reject unauthorized cancellation: " + ex.getMessage());

        // Alice cancels her own order -> succeeds
        Order cancelled = tradingService.cancelOrder(alice.getUserId(), orderId);
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    @DisplayName("Portfolio Valuation: Correctly computes cash, holdings market value, and total net worth")
    void testPortfolioValuation() throws Exception {
        User charlie = userDAO.findByUsername("charlie");
        Map<String, Object> summary = portfolioService.getPortfolioSummary(charlie.getUserId());

        assertNotNull(summary);
        assertEquals("charlie", summary.get("username"));
        double cash = ((Number) summary.get("cashBalance")).doubleValue();
        double holdingsVal = ((Number) summary.get("holdingsMarketValue")).doubleValue();
        double netWorth = ((Number) summary.get("totalNetWorth")).doubleValue();

        assertEquals(cash + holdingsVal, netWorth, 0.01, "Net worth must equal cash + holdings value");
    }
}
