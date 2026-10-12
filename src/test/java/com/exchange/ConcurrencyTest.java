package com.exchange;

import com.exchange.engine.MatchingEngine;
import com.exchange.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrencyTest {

    @Test
    @DisplayName("Multithreading (Slide 08): High-concurrency simultaneous order submissions prevent race conditions")
    void testConcurrentOrderMatching() throws InterruptedException {
        MatchingEngine engine = new MatchingEngine();
        String symbol = "AAPL";
        int numThreads = 16;
        int ordersPerThread = 50;
        int totalExpectedOrders = numThreads * ordersPerThread;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numThreads);

        AtomicInteger totalTradesCount = new AtomicInteger(0);
        AtomicInteger totalTradedShares = new AtomicInteger(0);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Synchronize all threads to start simultaneously
                    for (int j = 0; j < ordersPerThread; j++) {
                        String orderId = String.format("t%d-ord%d", threadId, j);
                        String userId = "trader-" + threadId;
                        // Half buyers, half sellers
                        if (threadId % 2 == 0) {
                            BuyOrder buy = new BuyOrder(orderId, userId, symbol, OrderType.LIMIT, 200.0, 10, System.currentTimeMillis());
                            MatchResult res = engine.processOrder(buy);
                            totalTradesCount.addAndGet(res.getTrades().size());
                            totalTradedShares.addAndGet(res.getTotalSharesExecuted());
                        } else {
                            SellOrder sell = new SellOrder(orderId, userId, symbol, OrderType.LIMIT, 200.0, 10, System.currentTimeMillis());
                            MatchResult res = engine.processOrder(sell);
                            totalTradesCount.addAndGet(res.getTrades().size());
                            totalTradedShares.addAndGet(res.getTotalSharesExecuted());
                        }
                    }
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Launch all threads!
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS), "All threads should finish within 10s");
        executor.shutdown();

        assertTrue(errors.isEmpty(), "Concurrency execution should produce zero exceptions: " + errors);

        // Verify state consistency
        OrderBookSnapshot snapshot = engine.getSnapshot(symbol, 10);
        assertNotNull(snapshot);

        int restingBidShares = snapshot.getBids().stream().mapToInt(PriceLevel::getQuantity).sum();
        int restingAskShares = snapshot.getAsks().stream().mapToInt(PriceLevel::getQuantity).sum();

        // 8 buyer threads * 50 orders * 10 shares = 4000 buy shares
        // 8 seller threads * 50 orders * 10 shares = 4000 sell shares
        // They should perfectly match against each other!
        int expectedTotalShares = (numThreads / 2) * ordersPerThread * 10;
        assertEquals(expectedTotalShares, totalTradedShares.get(),
                "All matching buy and sell shares must match completely without dropped trades");
        assertEquals(0, restingBidShares, "No unmatched bids should remain");
        assertEquals(0, restingAskShares, "No unmatched asks should remain");
    }
}
