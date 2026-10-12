package com.exchange;

import com.exchange.engine.OrderBook;
import com.exchange.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookTest {
    private OrderBook orderBook;

    @BeforeEach
    void setUp() {
        orderBook = new OrderBook("AAPL");
    }

    @Test
    @DisplayName("Bids should prioritize highest price first, then FIFO timestamp")
    void testBidPriceTimePriority() {
        long t0 = 1000L;
        BuyOrder b1 = new BuyOrder("b1", "u1", "AAPL", OrderType.LIMIT, 199.70, 100, t0);
        BuyOrder b2 = new BuyOrder("b2", "u2", "AAPL", OrderType.LIMIT, 199.90, 100, t0 + 10);
        BuyOrder b3 = new BuyOrder("b3", "u3", "AAPL", OrderType.LIMIT, 199.80, 100, t0 + 20);
        BuyOrder b4 = new BuyOrder("b4", "u4", "AAPL", OrderType.LIMIT, 199.90, 50, t0 + 30); // Same price as b2, later time

        orderBook.addOrder(b1);
        orderBook.addOrder(b2);
        orderBook.addOrder(b3);
        orderBook.addOrder(b4);

        // First polled should be b2 (highest price 199.90, earlier time)
        assertEquals("b2", orderBook.pollBestBid().getOrderId());
        // Second polled should be b4 (highest price 199.90, later time)
        assertEquals("b4", orderBook.pollBestBid().getOrderId());
        // Third polled should be b3 (199.80)
        assertEquals("b3", orderBook.pollBestBid().getOrderId());
        // Fourth polled should be b1 (199.70)
        assertEquals("b1", orderBook.pollBestBid().getOrderId());
    }

    @Test
    @DisplayName("Asks should prioritize lowest price first, then FIFO timestamp")
    void testAskPriceTimePriority() {
        long t0 = 1000L;
        SellOrder s1 = new SellOrder("s1", "u1", "AAPL", OrderType.LIMIT, 200.50, 100, t0);
        SellOrder s2 = new SellOrder("s2", "u2", "AAPL", OrderType.LIMIT, 200.10, 50, t0 + 10);
        SellOrder s3 = new SellOrder("s3", "u3", "AAPL", OrderType.LIMIT, 200.20, 100, t0 + 20);
        SellOrder s4 = new SellOrder("s4", "u4", "AAPL", OrderType.LIMIT, 200.10, 75, t0 + 30); // Same price as s2, later time

        orderBook.addOrder(s1);
        orderBook.addOrder(s2);
        orderBook.addOrder(s3);
        orderBook.addOrder(s4);

        // First polled: s2 (lowest price 200.10, earlier time)
        assertEquals("s2", orderBook.pollBestAsk().getOrderId());
        // Second polled: s4 (lowest price 200.10, later time)
        assertEquals("s4", orderBook.pollBestAsk().getOrderId());
        // Third polled: s3 (200.20)
        assertEquals("s3", orderBook.pollBestAsk().getOrderId());
        // Fourth polled: s1 (200.50)
        assertEquals("s1", orderBook.pollBestAsk().getOrderId());
    }

    @Test
    @DisplayName("Order cancellation removes order from book and sets status to CANCELLED")
    void testOrderCancellation() {
        BuyOrder b1 = new BuyOrder("b-cancel", "u1", "AAPL", OrderType.LIMIT, 199.50, 100, 1000L);
        orderBook.addOrder(b1);
        assertEquals(1, orderBook.getBidCount());

        Order cancelled = orderBook.cancelOrder("b-cancel");
        assertNotNull(cancelled);
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        assertEquals(0, orderBook.getBidCount());
        assertNull(orderBook.peekBestBid());
    }

    @Test
    @DisplayName("Aggregated Snapshot computes price levels, depth, and spread accurately")
    void testOrderBookSnapshot() {
        // Presentation Slide 06 baseline
        orderBook.addOrder(new BuyOrder("b1", "u1", "AAPL", OrderType.LIMIT, 199.90, 100, 1L));
        orderBook.addOrder(new BuyOrder("b2", "u2", "AAPL", OrderType.LIMIT, 199.80, 200, 2L));
        orderBook.addOrder(new BuyOrder("b3", "u3", "AAPL", OrderType.LIMIT, 199.70, 150, 3L));

        orderBook.addOrder(new SellOrder("s1", "u4", "AAPL", OrderType.LIMIT, 200.10, 50, 4L));
        orderBook.addOrder(new SellOrder("s2", "u5", "AAPL", OrderType.LIMIT, 200.20, 100, 5L));
        orderBook.addOrder(new SellOrder("s3", "u6", "AAPL", OrderType.LIMIT, 200.50, 200, 6L));

        OrderBookSnapshot snapshot = orderBook.getSnapshot(10);
        assertEquals("AAPL", snapshot.getSymbol());
        assertEquals(199.90, snapshot.getBestBid(), 0.001);
        assertEquals(200.10, snapshot.getBestAsk(), 0.001);
        assertEquals(0.20, snapshot.getSpread(), 0.001);

        assertEquals(3, snapshot.getBids().size());
        assertEquals(3, snapshot.getAsks().size());
        assertEquals(100, snapshot.getBids().get(0).getQuantity());
        assertEquals(50, snapshot.getAsks().get(0).getQuantity());
    }
}
