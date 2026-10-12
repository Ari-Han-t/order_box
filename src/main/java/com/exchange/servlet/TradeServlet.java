package com.exchange.servlet;

import com.exchange.model.Trade;
import com.exchange.service.TradingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Servlet exposing recently executed trades and market tape
 * (Presentation Slide 10 / 12).
 */
public class TradeServlet extends BaseServlet {
    private final TradingService tradingService;

    public TradeServlet(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String symbol = req.getParameter("symbol");
            if (symbol == null || symbol.trim().isEmpty()) {
                symbol = "AAPL";
            }

            int limit = 25;
            String limitParam = req.getParameter("limit");
            if (limitParam != null) {
                try {
                    limit = Integer.parseInt(limitParam);
                } catch (NumberFormatException ignored) {}
            }

            List<Trade> trades = tradingService.getRecentTrades(symbol, limit);
            sendJson(resp, HttpServletResponse.SC_OK, trades);
        } catch (Exception ex) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve trades: " + ex.getMessage());
        }
    }
}
