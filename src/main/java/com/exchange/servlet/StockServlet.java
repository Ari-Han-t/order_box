package com.exchange.servlet;

import com.exchange.dao.StockDAO;
import com.exchange.model.Stock;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Servlet exposing listed Stocks and securities quotes.
 */
public class StockServlet extends BaseServlet {
    private final StockDAO stockDAO;

    public StockServlet(StockDAO stockDAO) {
        this.stockDAO = stockDAO;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String symbol = req.getParameter("symbol");
            if (symbol != null && !symbol.trim().isEmpty()) {
                Stock stock = stockDAO.findBySymbol(symbol);
                if (stock == null) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Stock not found: " + symbol);
                    return;
                }
                sendJson(resp, HttpServletResponse.SC_OK, stock);
            } else {
                List<Stock> stocks = stockDAO.findAllStocks();
                sendJson(resp, HttpServletResponse.SC_OK, stocks);
            }
        } catch (Exception ex) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve stocks: " + ex.getMessage());
        }
    }
}
