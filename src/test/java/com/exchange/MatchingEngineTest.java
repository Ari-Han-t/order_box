package com.exchange;

import com.exchange.engine.MatchingEngine;
import com.exchange.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineTest {
    private MatchingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new MatchingEngine();
    }

    @Test
    @DisplayName("Presentation Slides 07 & 11: Maker-Price Matching and Partial Execution")
    void testPresentationMatchingScenario() {
        // Step 1: Bob places resting Ask: SELL 50 shares @ $199.80
        SellOrder bobAsk = new SellOrder("ord-bob-1", "user-bob", "AAPL", OrderType.LIMIT, 199.80, 50, 1000L);
        MatchResult bobRes = engine.processOrder(bobAsk);
        assertEquals(0, bobRes.getTrades().size(), "Resting order should have no immediate counterparty");
        assertEquals(OrderStatus.PENDING, bobAsk.getStatus());

        // Step 2: Alice submits incoming Bid: BUY 100 shares @ $200.00
        BuyOrder aliceBid = new BuyOrder("ord-alice-1", "user-alice", "AAPL", OrderType.LIMIT, 200.00, 100, 1050L);
        MatchResult aliceRes = engine.processOrder(aliceBid);

        // Verify Trade Execution
        List<Trade> trades = aliceRes.getTrades();
        assertEquals(1, trades.size(), "Should produce exactly 1 trade");
        Trade trade = trades.get(0);

        // Maker Price Rule: Trade executes at Bob's resting ask price ($199.80), NOT Alice's $200.00!
        assertEquals(199.80, trade.getPrice(), 0.001);
        assertEquals(50, trade.getQuantity());
        assertEquals(9990.00, trade.getTotalAmount(), 0.01);
        assertEquals("user-alice", trade.getBuyerId());
        assertEquals("user-bob", trade.getSellerId());

        // Bob's Ask should be completely FILLED
        assertEquals(OrderStatus.FILLED, bobAsk.getStatus());
        assertEquals(0, bobAsk.getRemainingQuantity());

        // Alice's Bid should be PARTIALLY_FILLED with 50 shares remaining
        assertEquals(OrderStatus.PARTIALLY_FILLED, aliceBid.getStatus());
        assertEquals(50, aliceBid.getRemainingQuantity());

        // Order Book should now show Alice's remaining 50 shares @ $200.00 as the top bid!
        OrderBookSnapshot snapshot = engine.getSnapshot("AAPL", 5);
        assertEquals(200.00, snapshot.getBestBid(), 0.001);
        assertEquals(50, snapshot.getBids().get(0).getQuantity());
        assertEquals(0.0, snapshot.getBestAsk(), 0.001, "All asks were consumed");
    }

    @Test
    @DisplayName("Multiple price level matching: Incoming large order sweeps depth")
    void testSweepMultiplePriceLevels() {
        // Resting asks: 50 @ 100.0, 50 @ 101.0, 100 @ 102.0
        engine.processOrder(new SellOrder("s1", "u1", "MSFT", OrderType.LIMIT, 100.0, 50, 100L));
        engine.processOrder(new SellOrder("s2", "u2", "MSFT", OrderType.LIMIT, 101.0, 50, 200L));
        engine.processOrder(new SellOrder("s3", "u3", "MSFT", OrderType.LIMIT, 102.0, 100, 300L));

        // Incoming Buy for 120 shares @ 102.0
        BuyOrder buy = new BuyOrder("b-big", "buyer", "MSFT", OrderType.LIMIT, 102.0, 120, 400L);
        MatchResult res = engine.processOrder(buy);

        assertEquals(3, res.getTrades().size());
        assertEquals(120, res.getTotalSharesExecuted());
        assertTrue(res.isFullyFilled());
        assertEquals(OrderStatus.FILLED, buy.getStatus());

        // Remaining ask on the book should be s3 with 80 shares @ 102.0
        OrderBookSnapshot snapshot = engine.getSnapshot("MSFT", 5);
        assertEquals(102.0, snapshot.getBestAsk(), 0.001);
        assertEquals(80, snapshot.getAsks().get(0).getQuantity());
    }

    @Test
    @DisplayName("Market Buy Order executes immediately at best available resting Ask")
    void testMarketOrderExecution() {
        engine.processOrder(new SellOrder("s-mkt", "seller", "TSLA", OrderType.LIMIT, 250.0, 40, 100L));

        BuyOrder marketBuy = new BuyOrder("b-mkt", "buyer", "TSLA", OrderType.MARKET, 0.0, 20, 200L);
        MatchResult res = engine.processOrder(marketBuy);

        assertEquals(1, res.getTrades().size());
        assertEquals(250.0, res.getTrades().get(0).getPrice(), 0.001);
        assertEquals(20, res.getTrades().get(0).getQuantity());
        assertEquals(OrderStatus.FILLED, marketBuy.getStatus());
    }
}
