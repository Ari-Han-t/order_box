package com.exchange.service;

import com.exchange.dao.PortfolioDAO;
import com.exchange.dao.StockDAO;
import com.exchange.dao.UserDAO;
import com.exchange.model.Holding;
import com.exchange.model.Portfolio;
import com.exchange.model.Stock;
import com.exchange.model.User;

import java.sql.SQLException;
import java.util.*;

/**
 * Service managing user portfolios, cash balances, and position valuations.
 */
public class PortfolioService {
    private final PortfolioDAO portfolioDAO;
    private final UserDAO userDAO;
    private final StockDAO stockDAO;

    public PortfolioService(PortfolioDAO portfolioDAO, UserDAO userDAO, StockDAO stockDAO) {
        this.portfolioDAO = portfolioDAO;
        this.userDAO = userDAO;
        this.stockDAO = stockDAO;
    }

    public Portfolio getPortfolio(String userId) throws SQLException {
        return portfolioDAO.getPortfolio(userId);
    }

    public Map<String, Object> getPortfolioSummary(String userId) throws SQLException {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        Portfolio portfolio = portfolioDAO.getPortfolio(userId);
        List<Stock> allStocks = stockDAO.findAllStocks();
        Map<String, Stock> stockMap = new HashMap<>();
        Map<String, Double> priceMap = new HashMap<>();
        for (Stock s : allStocks) {
            stockMap.put(s.getSymbol(), s);
            priceMap.put(s.getSymbol(), s.getCurrentPrice());
        }

        double holdingsMarketValue = 0.0;
        double totalCostBasis = 0.0;
        List<Map<String, Object>> holdingDetails = new ArrayList<>();

        for (Holding h : portfolio.getHoldingsList()) {
            Stock stock = stockMap.get(h.getSymbol());
            double currentPrice = stock != null ? stock.getCurrentPrice() : h.getAverageBuyPrice();
            double marketValue = h.getMarketValue(currentPrice);
            double costBasis = h.getCostBasis();
            double pnl = h.getUnrealizedPnL(currentPrice);
            double returnPct = h.getReturnPercent(currentPrice);

            holdingsMarketValue += marketValue;
            totalCostBasis += costBasis;

            Map<String, Object> hd = new LinkedHashMap<>();
            hd.put("symbol", h.getSymbol());
            hd.put("companyName", stock != null ? stock.getCompanyName() : h.getSymbol());
            hd.put("quantity", h.getQuantity());
            hd.put("averageBuyPrice", h.getAverageBuyPrice());
            hd.put("currentPrice", currentPrice);
            hd.put("marketValue", marketValue);
            hd.put("unrealizedPnL", pnl);
            hd.put("returnPercent", returnPct);
            holdingDetails.add(hd);
        }

        double cashBalance = user.getBalance();
        double totalNetWorth = cashBalance + holdingsMarketValue;
        double totalPnL = holdingsMarketValue - totalCostBasis;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());
        result.put("fullName", user.getFullName());
        result.put("cashBalance", cashBalance);
        result.put("holdingsMarketValue", holdingsMarketValue);
        result.put("totalNetWorth", totalNetWorth);
        result.put("totalPnL", totalPnL);
        result.put("holdings", holdingDetails);

        return result;
    }
}
