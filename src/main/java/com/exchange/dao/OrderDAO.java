package com.exchange.dao;

import com.exchange.db.DatabaseManager;
import com.exchange.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Orders.
 * Persists and reconstructs polymorphic {@link BuyOrder} and {@link SellOrder}
 * objects using JDBC (Presentation Slide 09 / 12).
 */
public class OrderDAO {
    private final DatabaseManager dbManager;

    public OrderDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void saveOrder(Order order) throws SQLException {
        try (Connection conn = dbManager.getConnection()) {
            saveOrder(conn, order);
        }
    }

    public void saveOrder(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO orders (order_id, user_id, symbol, side, type, price, quantity, " +
                     "remaining_quantity, status, timestamp_epoch, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.getOrderId());
            ps.setString(2, order.getUserId());
            ps.setString(3, order.getSymbol());
            ps.setString(4, order.getSide().name());
            ps.setString(5, order.getType().name());
            ps.setDouble(6, order.getPrice());
            ps.setInt(7, order.getQuantity());
            ps.setInt(8, order.getRemainingQuantity());
            ps.setString(9, order.getStatus().name());
            ps.setLong(10, order.getTimestampEpoch());
            ps.executeUpdate();
        }
    }

    public void updateOrder(Order order) throws SQLException {
        try (Connection conn = dbManager.getConnection()) {
            updateOrder(conn, order);
        }
    }

    public void updateOrder(Connection conn, Order order) throws SQLException {
        String sql = "UPDATE orders SET remaining_quantity = ?, status = ? WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, order.getRemainingQuantity());
            ps.setString(2, order.getStatus().name());
            ps.setString(3, order.getOrderId());
            ps.executeUpdate();
        }
    }

    public Order findById(String orderId) throws SQLException {
        String sql = "SELECT order_id, user_id, symbol, side, type, price, quantity, remaining_quantity, " +
                     "status, timestamp_epoch FROM orders WHERE order_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToOrder(rs);
                }
            }
        }
        return null;
    }

    public List<Order> findActiveOrdersByUserId(String userId) throws SQLException {
        String sql = "SELECT order_id, user_id, symbol, side, type, price, quantity, remaining_quantity, " +
                     "status, timestamp_epoch FROM orders WHERE user_id = ? AND status IN ('PENDING', 'PARTIALLY_FILLED') " +
                     "ORDER BY timestamp_epoch DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToOrder(rs));
                }
            }
        }
        return list;
    }

    public List<Order> findOrdersByUserId(String userId, int limit) throws SQLException {
        String sql = "SELECT order_id, user_id, symbol, side, type, price, quantity, remaining_quantity, " +
                     "status, timestamp_epoch FROM orders WHERE user_id = ? " +
                     "ORDER BY timestamp_epoch DESC LIMIT ?";
        List<Order> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setInt(2, limit > 0 ? limit : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToOrder(rs));
                }
            }
        }
        return list;
    }

    public List<Order> findAllPendingOrders() throws SQLException {
        String sql = "SELECT order_id, user_id, symbol, side, type, price, quantity, remaining_quantity, " +
                     "status, timestamp_epoch FROM orders WHERE status IN ('PENDING', 'PARTIALLY_FILLED') " +
                     "ORDER BY timestamp_epoch ASC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToOrder(rs));
            }
        }
        return list;
    }

    /**
     * Polymorphically maps a database row into either a BuyOrder or SellOrder instance.
     */
    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        String orderId = rs.getString("order_id");
        String userId = rs.getString("user_id");
        String symbol = rs.getString("symbol");
        String sideStr = rs.getString("side");
        String typeStr = rs.getString("type");
        double price = rs.getDouble("price");
        int quantity = rs.getInt("quantity");
        int remainingQuantity = rs.getInt("remaining_quantity");
        String statusStr = rs.getString("status");
        long timestampEpoch = rs.getLong("timestamp_epoch");

        OrderType type = OrderType.valueOf(typeStr);
        OrderStatus status = OrderStatus.valueOf(statusStr);

        if ("BUY".equalsIgnoreCase(sideStr)) {
            return new BuyOrder(orderId, userId, symbol, type, price, quantity, remainingQuantity, status, timestampEpoch);
        } else {
            return new SellOrder(orderId, userId, symbol, type, price, quantity, remainingQuantity, status, timestampEpoch);
        }
    }
}
