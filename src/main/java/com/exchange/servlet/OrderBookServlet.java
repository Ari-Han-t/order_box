package com.exchange.servlet;

import com.exchange.model.OrderBookSnapshot;
import com.exchange.service.TradingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet exposing the real-time aggregated Order Book for a stock symbol
 * (Presentation Slide 10 / 12 & Slide 06 / 12).
 */
public class OrderBookServlet extends BaseServlet {
    private final TradingService tradingService;

    public OrderBookServlet(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String symbol = req.getParameter("symbol");
            if (symbol == null || symbol.trim().isEmpty()) {
                symbol = "AAPL"; // Default to Apple for demo convenience
            }

            int depth = 15;
            String depthParam = req.getParameter("depth");
            if (depthParam != null) {
                try {
                    depth = Integer.parseInt(depthParam);
                } catch (NumberFormatException ignored) {}
            }

            OrderBookSnapshot snapshot = tradingService.getOrderBookSnapshot(symbol, depth);
            sendJson(resp, HttpServletResponse.SC_OK, snapshot);
        } catch (Exception ex) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve order book: " + ex.getMessage());
        }
    }
}
