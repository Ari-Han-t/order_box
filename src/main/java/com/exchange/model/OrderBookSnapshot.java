package com.exchange.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable point-in-time snapshot of the Order Book for a stock symbol,
 * used for JavaFX UI rendering and REST API /orderbook responses.
 */
public class OrderBookSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String symbol;
    private final long timestamp;
    private final List<PriceLevel> bids;
    private final List<PriceLevel> asks;
    private final double bestBid;
    private final double bestAsk;
    private final double spread;
    private final double lastPrice;

    @JsonCreator
    public OrderBookSnapshot(@JsonProperty("symbol") String symbol,
                             @JsonProperty("timestamp") long timestamp,
                             @JsonProperty("bids") List<PriceLevel> bids,
                             @JsonProperty("asks") List<PriceLevel> asks,
                             @JsonProperty("lastPrice") double lastPrice) {
        this.symbol = symbol;
        this.timestamp = timestamp;
        this.bids = bids != null ? Collections.unmodifiableList(new ArrayList<>(bids)) : Collections.emptyList();
        this.asks = asks != null ? Collections.unmodifiableList(new ArrayList<>(asks)) : Collections.emptyList();
        this.lastPrice = lastPrice;

        this.bestBid = !this.bids.isEmpty() ? this.bids.get(0).getPrice() : 0.0;
        this.bestAsk = !this.asks.isEmpty() ? this.asks.get(0).getPrice() : 0.0;
        this.spread = (bestBid > 0.0 && bestAsk > 0.0) ? Math.max(0.0, bestAsk - bestBid) : 0.0;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<PriceLevel> getBids() {
        return bids;
    }

    public List<PriceLevel> getAsks() {
        return asks;
    }

    public double getBestBid() {
        return bestBid;
    }

    public double getBestAsk() {
        return bestAsk;
    }

    public double getSpread() {
        return spread;
    }

    public double getLastPrice() {
        return lastPrice;
    }
}
