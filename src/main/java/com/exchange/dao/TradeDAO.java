package com.exchange.dao;

import com.exchange.db.DatabaseManager;
import com.exchange.model.Trade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Executed Trades.
 * Maintains transaction history and trade records (Presentation Slide 09 / 12).
 */
public class TradeDAO {
    private final DatabaseManager dbManager;

    public TradeDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void saveTrade(Trade trade) throws SQLException {
        try (Connection conn = dbManager.getConnection()) {
            saveTrade(conn, trade);
        }
    }

    public void saveTrade(Connection conn, Trade trade) throws SQLException {
        String sql = "INSERT INTO trades (trade_id, symbol, buy_order_id, sell_order_id, buyer_id, seller_id, " +
                     "price, quantity, total_amount, executed_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trade.getTradeId());
            ps.setString(2, trade.getSymbol());
            ps.setString(3, trade.getBuyOrderId());
            ps.setString(4, trade.getSellOrderId());
            ps.setString(5, trade.getBuyerId());
            ps.setString(6, trade.getSellerId());
            ps.setDouble(7, trade.getPrice());
            ps.setInt(8, trade.getQuantity());
            ps.setDouble(9, trade.getTotalAmount());
            ps.executeUpdate();
        }
    }

    public List<Trade> findRecentTradesBySymbol(String symbol, int limit) throws SQLException {
        String sql = "SELECT trade_id, symbol, buy_order_id, sell_order_id, buyer_id, seller_id, " +
                     "price, quantity, total_amount, executed_at FROM trades " +
                     "WHERE symbol = ? ORDER BY executed_at DESC, trade_id DESC LIMIT ?";
        List<Trade> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, symbol.toUpperCase().trim());
            ps.setInt(2, limit > 0 ? limit : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToTrade(rs));
                }
            }
        }
        return list;
    }

    public List<Trade> findTradesByUserId(String userId, int limit) throws SQLException {
        String sql = "SELECT trade_id, symbol, buy_order_id, sell_order_id, buyer_id, seller_id, " +
                     "price, quantity, total_amount, executed_at FROM trades " +
                     "WHERE buyer_id = ? OR seller_id = ? ORDER BY executed_at DESC LIMIT ?";
        List<Trade> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, userId);
            ps.setInt(3, limit > 0 ? limit : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToTrade(rs));
                }
            }
        }
        return list;
    }

    public List<Trade> findAllRecentTrades(int limit) throws SQLException {
        String sql = "SELECT trade_id, symbol, buy_order_id, sell_order_id, buyer_id, seller_id, " +
                     "price, quantity, total_amount, executed_at FROM trades " +
                     "ORDER BY executed_at DESC LIMIT ?";
        List<Trade> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToTrade(rs));
                }
            }
        }
        return list;
    }

    private Trade mapRowToTrade(ResultSet rs) throws SQLException {
        long epoch = rs.getTimestamp("executed_at") != null ?
                rs.getTimestamp("executed_at").getTime() : System.currentTimeMillis();
        return new Trade(
                rs.getString("trade_id"),
                rs.getString("symbol"),
                rs.getString("buy_order_id"),
                rs.getString("sell_order_id"),
                rs.getString("buyer_id"),
                rs.getString("seller_id"),
                rs.getDouble("price"),
                rs.getInt("quantity"),
                epoch
        );
    }
}
