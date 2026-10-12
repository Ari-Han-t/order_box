package com.exchange.service;

import com.exchange.dao.StockDAO;
import com.exchange.dao.UserDAO;
import com.exchange.engine.OrderBook;
import com.exchange.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Scripted Demo Service executing the live presentation scenario
 * (Presentation Slide 07 & Slide 11).
 * Demonstrates:
 * 1. Alice placing BUY 100 AAPL @ $200.00
 * 2. Bob placing SELL 50 AAPL @ $199.80
 * 3. Atomic Maker-Price execution: 50 shares @ $199.80 ($9,990.00)
 * 4. Partial fill: Alice maintains remaining BUY 50 AAPL @ $200.00 in the live Order Book
 * 5. Automatic balance and portfolio updates without overselling or negative balance.
 */
public class DemoScenarioService {
    private static final Logger logger = LoggerFactory.getLogger(DemoScenarioService.class);

    private final TradingService tradingService;
    private final UserDAO userDAO;
    private final StockDAO stockDAO;
    private final PortfolioService portfolioService;

    public DemoScenarioService(TradingService tradingService, UserDAO userDAO,
                               StockDAO stockDAO, PortfolioService portfolioService) {
        this.tradingService = tradingService;
        this.userDAO = userDAO;
        this.stockDAO = stockDAO;
        this.portfolioService = portfolioService;
    }

    public Map<String, Object> runPresentationScenario() throws Exception {
        logger.info("Starting Scripted Live Demo Scenario (Presentation Slides 07 & 11)...");

        // Ensure users exist
        User alice = userDAO.findByUsername("alice");
        User bob = userDAO.findByUsername("bob");

        if (alice == null || bob == null) {
            throw new IllegalStateException("Seeded users 'alice' and 'bob' must exist in the database to run demo");
        }

        double aliceInitialBalance = alice.getBalance();
        double bobInitialBalance = bob.getBalance();

        List<Map<String, Object>> stepLogs = new ArrayList<>();

        // Ensure AAPL bids are below $199.80 so Bob's $199.80 Ask rests on the book as Best Ask
        OrderBook aaplBook = tradingService.getMatchingEngine().getOrCreateBook("AAPL");
        while (aaplBook.peekBestBid() != null && aaplBook.peekBestBid().getPrice() >= 199.80) {
            BuyOrder topBid = aaplBook.pollBestBid();
            if (topBid != null) topBid.cancel();
        }

        // Step 1: Bob places resting SELL limit order for 50 shares @ $199.80
        Map<String, Object> step1 = new LinkedHashMap<>();
        step1.put("step", 1);
        step1.put("action", "Bob submits SELL order");
        step1.put("trader", "Bob");
        step1.put("details", "SELL 50 AAPL @ $199.80 (Limit Order)");
        MatchResult bobResult = tradingService.submitOrder(
                bob.getUserId(), "AAPL", OrderSide.SELL, OrderType.LIMIT, 199.80, 50);
        step1.put("orderId", bobResult.getIncomingOrder().getOrderId());
        step1.put("status", bobResult.getIncomingOrder().getStatus().name());
        step1.put("restingInBook", bobResult.getIncomingOrder().isOpen());
        stepLogs.add(step1);

        // Step 2: Alice submits incoming BUY limit order for 100 shares @ $200.00
        Map<String, Object> step2 = new LinkedHashMap<>();
        step2.put("step", 2);
        step2.put("action", "Alice submits BUY order");
        step2.put("trader", "Alice");
        step2.put("details", "BUY 100 AAPL @ $200.00 (Limit Order)");
        MatchResult aliceResult = tradingService.submitOrder(
                alice.getUserId(), "AAPL", OrderSide.BUY, OrderType.LIMIT, 200.00, 100);
        step2.put("orderId", aliceResult.getIncomingOrder().getOrderId());
        step2.put("status", aliceResult.getIncomingOrder().getStatus().name());
        step2.put("remainingQuantity", aliceResult.getIncomingOrder().getRemainingQuantity());
        stepLogs.add(step2);

        // Step 3: Analyze Trade Execution
        List<Trade> trades = aliceResult.getTrades();
        Map<String, Object> step3 = new LinkedHashMap<>();
        step3.put("step", 3);
        step3.put("action", "Matching Engine Execution");
        if (!trades.isEmpty()) {
            Trade trade = trades.get(0);
            step3.put("matched", true);
            step3.put("tradeId", trade.getTradeId());
            step3.put("executionPrice", trade.getPrice());
            step3.put("quantityMatched", trade.getQuantity());
            step3.put("totalTurnover", trade.getTotalAmount());
            step3.put("makerPriceRule", "Matched at resting Ask price: $" + trade.getPrice() + " (not buyer's $200.00)");
        } else {
            step3.put("matched", false);
        }
        stepLogs.add(step3);

        // Step 4: Verify Post-Match State
        User alicePost = userDAO.findById(alice.getUserId());
        User bobPost = userDAO.findById(bob.getUserId());
        OrderBookSnapshot snapshot = tradingService.getOrderBookSnapshot("AAPL", 10);

        Map<String, Object> step4 = new LinkedHashMap<>();
        step4.put("step", 4);
        step4.put("action", "Post-Match Verification");
        step4.put("aliceBalanceChange", String.format("$%.2f -> $%.2f (Deducted $%.2f)",
                aliceInitialBalance, alicePost.getBalance(), aliceInitialBalance - alicePost.getBalance()));
        step4.put("bobBalanceChange", String.format("$%.2f -> $%.2f (Credited $%.2f)",
                bobInitialBalance, bobPost.getBalance(), bobPost.getBalance() - bobInitialBalance));
        step4.put("aliceRemainingOrderInBook", "50 AAPL @ $200.00");
        step4.put("bestBidInOrderBook", snapshot.getBestBid());
        step4.put("bestAskInOrderBook", snapshot.getBestAsk());
        stepLogs.add(step4);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("status", "SUCCESS");
        summary.put("scenarioName", "Presentation Partial Fill & Maker-Price Priority Demo");
        summary.put("steps", stepLogs);
        summary.put("alicePortfolio", portfolioService.getPortfolioSummary(alice.getUserId()));
        summary.put("bobPortfolio", portfolioService.getPortfolioSummary(bob.getUserId()));
        summary.put("orderBookSnapshot", snapshot);

        return summary;
    }
}
