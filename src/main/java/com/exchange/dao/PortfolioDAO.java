package com.exchange.dao;

import com.exchange.db.DatabaseManager;
import com.exchange.model.Holding;
import com.exchange.model.Portfolio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Data Access Object for User Portfolios and position tracking (Presentation Slide 09 / 12).
 */
public class PortfolioDAO {
    private final DatabaseManager dbManager;

    public PortfolioDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public Portfolio getPortfolio(String userId) throws SQLException {
        Portfolio portfolio = new Portfolio(userId);
        String sql = "SELECT symbol, quantity, average_buy_price FROM portfolios WHERE user_id = ? AND quantity > 0";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String symbol = rs.getString("symbol");
                    int quantity = rs.getInt("quantity");
                    double avgPrice = rs.getDouble("average_buy_price");
                    portfolio.setHolding(new Holding(symbol, quantity, avgPrice));
                }
            }
        }
        return portfolio;
    }

    public Holding getHolding(String userId, String symbol) throws SQLException {
        String sql = "SELECT symbol, quantity, average_buy_price FROM portfolios WHERE user_id = ? AND symbol = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, symbol.toUpperCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Holding(
                            rs.getString("symbol"),
                            rs.getInt("quantity"),
                            rs.getDouble("average_buy_price")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Atomically adjusts holdings during trade execution within a transaction.
     * Computes weighted average price on buy; decrements quantity on sell.
     */
    public void adjustHolding(Connection conn, String userId, String symbol, int deltaShares, double tradePrice) throws SQLException {
        String sym = symbol.toUpperCase().trim();
        String selectSql = "SELECT quantity, average_buy_price FROM portfolios WHERE user_id = ? AND symbol = ?";

        int currentQty = 0;
        double currentAvg = 0.0;
        boolean exists = false;

        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, userId);
            ps.setString(2, sym);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    exists = true;
                    currentQty = rs.getInt("quantity");
                    currentAvg = rs.getDouble("average_buy_price");
                }
            }
        }

        if (deltaShares > 0) {
            // Buying shares -> update weighted average price
            int newQty = currentQty + deltaShares;
            double newAvg = ((currentQty * currentAvg) + (deltaShares * tradePrice)) / newQty;

            if (exists) {
                String updateSql = "UPDATE portfolios SET quantity = ?, average_buy_price = ?, updated_at = CURRENT_TIMESTAMP " +
                                   "WHERE user_id = ? AND symbol = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, newQty);
                    ps.setDouble(2, newAvg);
                    ps.setString(3, userId);
                    ps.setString(4, sym);
                    ps.executeUpdate();
                }
            } else {
                String insertSql = "INSERT INTO portfolios (portfolio_id, user_id, symbol, quantity, average_buy_price, updated_at) " +
                                   "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, "port-" + UUID.randomUUID().toString().substring(0, 8));
                    ps.setString(2, userId);
                    ps.setString(3, sym);
                    ps.setInt(4, newQty);
                    ps.setDouble(5, newAvg);
                    ps.executeUpdate();
                }
            }
        } else if (deltaShares < 0) {
            // Selling shares -> reduce quantity, cost basis per share remains same
            int sellQty = Math.abs(deltaShares);
            if (currentQty < sellQty) {
                throw new IllegalStateException(String.format(
                        "Oversell rejected for user %s: attempting to sell %d %s shares with only %d held",
                        userId, sellQty, sym, currentQty));
            }

            int newQty = currentQty - sellQty;
            String updateSql = "UPDATE portfolios SET quantity = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ? AND symbol = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, newQty);
                ps.setString(2, userId);
                ps.setString(3, sym);
                ps.executeUpdate();
            }
        }
    }
}
